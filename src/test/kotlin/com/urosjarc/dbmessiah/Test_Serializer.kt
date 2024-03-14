package com.urosjarc.dbmessiah

import com.urosjarc.dbmessiah.data.*
import com.urosjarc.dbmessiah.domain.*
import com.urosjarc.dbmessiah.serializers.AllTS
import com.urosjarc.dbmessiah.serializers.NumberTS
import com.urosjarc.dbmessiah.serializers.StringTS
import com.urosjarc.dbmessiah.serializers.UUIDTS
import org.junit.jupiter.api.BeforeEach
import java.sql.JDBCType
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.test.Test
import kotlin.test.assertEquals

class Test_Serializer {
    lateinit var column: PrimaryColumn
    lateinit var tableInfo: TableInfo
    lateinit var procedureArg: ProcedureArg
    lateinit var procedure: Procedure
    lateinit var schema: Schema
    lateinit var ser: Serializer

    data class Child(val pk: Int, val fk: Int, val col: String)
    data class Parent(var pk: Int? = null, var col: String)
    data class UUIDChild(val pk: UUID, val fk: UUID, val col: String)
    data class UUIDParent(var pk: UUID? = null, var col: String)
    class TestProcedureEmpty
    data class TestProcedure(val parent_pk: Int, val parent_col: String)
    class SerializerImpl(
        allowAutoUUID: Boolean,
        schemas: List<Schema> = listOf(),
        globalSerializers: List<TypeSerializer<*>> = listOf(),
        globalInputs: List<KClass<*>> = listOf(),
        globalOutputs: List<KClass<*>> = listOf(),
        globalProcedures: List<KClass<*>> = listOf(),
    ) : Serializer(
        allowAutoUUID = allowAutoUUID,
        schemas = schemas,
        globalSerializers = globalSerializers,
        globalInputs = globalInputs,
        globalOutputs = globalOutputs,
        globalProcedures = globalProcedures,
    ) {
        override val selectLastId: String? = null
        override fun escaped(name: String): String = "'$name'"
        override fun <T : Any> createTable(table: KClass<T>): Query = TODO("Not yet implemented")
        override fun <T : Any> createProcedure(procedure: KClass<T>, procedureBody: String): Query = TODO("Not yet implemented")
        override fun <T : Any> callProcedure(procedure: T): Query = TODO("Not yet implemented")
        override fun <T : Any> dropProcedure(procedure: KClass<T>): Query = TODO("Not yet implemented")
    }

    @BeforeEach
    fun init() {
        this.column = PrimaryColumn(
            kprop = Parent::pk as KProperty1<Any, Any?>,
            dbType = "INTEGER",
            jdbcType = JDBCType.INTEGER,
            encoder = NumberTS.int.encoder,
            decoder = NumberTS.int.decoder
        )
        this.tableInfo = TableInfo(
            schema = "schema",
            primaryColumn = this.column,
            kclass = Parent::class,
            foreignColumns = listOf(),
            otherColumns = listOf(),
            typeSerializers = listOf()
        )
        this.schema = Schema(
            name = "schema",
            tables = listOf(
                Table(Parent::pk),
                Table(
                    Child::pk, foreignKeys = listOf(
                        Child::fk to Parent::class
                    )
                ),
                Table(UUIDParent::pk),
                Table(
                    UUIDChild::pk, foreignKeys = listOf(
                        UUIDChild::fk to UUIDParent::class
                    ), constraints = listOf(
                        UUIDChild::fk to listOf(C.CASCADE_DELETE)
                    )
                )
            ),
        )
        this.procedureArg = ProcedureArg(
            kprop = TestProcedure::parent_pk as KProperty1<Any, Any?>,
            dbType = "INTEGER",
            jdbcType = JDBCType.INTEGER,
            encoder = NumberTS.int.encoder,
            decoder = NumberTS.int.decoder
        )
        this.procedure = Procedure(
            schema = this.schema.name,
            kclass = TestProcedure::class,
            args = listOf(
                this.procedureArg
            )
        )
        this.ser = SerializerImpl(
            allowAutoUUID = true,
            schemas = listOf(this.schema),
            globalProcedures = listOf(TestProcedure::class, TestProcedureEmpty::class),
            globalSerializers = AllTS.sqlite,
        )
    }

    @Test
    fun `test escaped schema`() {
        assertEquals(actual = this.ser.escaped(schema = this.schema), expected = "'schema'")
    }

    @Test
    fun `test escaped procedure`() {
        assertEquals(actual = this.ser.escaped(procedure = this.procedure), expected = "'schema'.'TestProcedure'")
    }

    @Test
    fun `test escaped procedureArg`() {
        assertEquals(actual = this.ser.escaped(procedureArg = TestProcedure::parent_pk), expected = "'parent_pk'")
    }

    @Test
    fun `test escaped tableInfo`() {
        assertEquals(actual = this.ser.escaped(tableInfo = this.tableInfo), expected = "'schema'.'Parent'")
    }

    @Test
    fun `test escaped column`() {
        assertEquals(actual = this.ser.escaped(column = this.column), expected = "'schema'.'Parent'.'pk'")
    }

    @Test
    fun `test plantUML`() {
        assertEquals(
            actual = this.ser.plantUML(),
            expected = listOf(
                "@startuml",
                "skinparam backgroundColor darkgray",
                "skinparam ClassBackgroundColor lightgray",
                "",
                "package schema <<Folder>> {",
                "\t class schema.Parent {",
                "\t\t pk: Int",
                "\t }",
                "\t class schema.Child {",
                "\t\t pk: Int",
                "\t\t fk: Parent",
                "\t }",
                "\t class schema.UUIDParent {",
                "\t\t pk: UUID",
                "\t }",
                "\t class schema.UUIDChild {",
                "\t\t pk: UUID",
                "\t\t fk: UUIDParent",
                "\t }",
                "}",
                "",
                "schema.Child -down-> schema.Parent",
                "schema.UUIDChild -down-> schema.UUIDParent",
                "",
                "@enduml"
            ).joinToString("\n")
        )
    }

    @Test
    fun `test createSchema`() {
        assertEquals(
            actual = this.ser.createSchema(schema = this.schema),
            expected = Query(sql = "CREATE SCHEMA IF NOT EXISTS 'schema'")
        )
    }

    @Test
    fun `test dropSchema`() {
        assertEquals(
            actual = this.ser.dropSchema(schema = this.schema),
            expected = Query(sql = "DROP SCHEMA IF EXISTS 'schema'")
        )
        assertEquals(
            actual = this.ser.dropSchema(schema = this.schema, cascade = true),
            expected = Query(sql = "DROP SCHEMA IF EXISTS 'schema' CASCADE")
        )
    }

    @Test
    fun `test deleteTable`() {
        assertEquals(
            actual = this.ser.deleteTable(table = Parent::class),
            expected = Query(sql = "DELETE FROM 'schema'.'Parent'")
        )
    }

    @Test
    fun `test deleteRow`() {
        assertEquals(
            actual = this.ser.deleteRow(row = Parent(pk = 123, col = "col123")),
            expected = Query(
                sql = "DELETE FROM 'schema'.'Parent' WHERE 'schema'.'Parent'.'pk' = ?",
                QueryValue(name = "pk", value = 123, jdbcType = JDBCType.INTEGER, encoder = NumberTS.int.encoder)
            )
        )
    }

    @Test
    fun `test insertRow auto incremental`() {
        listOf(false, true).forEach {
            assertEquals(
                actual = this.ser.insertRow(row = Parent(col = "col123"), batch = it),
                expected = Query(
                    sql = "INSERT INTO 'schema'.'Parent' ('col') VALUES (?)",
                    QueryValue(name = "col", value = "col123", jdbcType = JDBCType.VARCHAR, encoder = StringTS.string(100).encoder)
                )
            )
            assertEquals(
                actual = this.ser.insertRow(row = UUIDParent(col = "col123"), batch = it),
                expected = Query(
                    sql = "INSERT INTO 'schema'.'UUIDParent' ('col') VALUES (?)",
                    QueryValue(name = "col", value = "col123", jdbcType = JDBCType.VARCHAR, encoder = StringTS.string(100).encoder)
                )
            )
        }
    }

    @Test
    fun `test insertRow non auto incremental`() {
        listOf(false, true).forEach {
            assertEquals(
                actual = this.ser.insertRow(row = Child(pk = 123, fk = 12, col = "col123"), batch = it),
                expected = Query(
                    sql = "INSERT INTO 'schema'.'Child' ('pk', 'fk', 'col') VALUES (?, ?, ?)",
                    QueryValue(name = "pk", value = 123, jdbcType = JDBCType.INTEGER, encoder = NumberTS.int.encoder),
                    QueryValue(name = "fk", value = 12, jdbcType = JDBCType.INTEGER, encoder = NumberTS.int.encoder),
                    QueryValue(name = "col", value = "col123", jdbcType = JDBCType.VARCHAR, encoder = StringTS.string(100).encoder)
                )
            )
            val row = UUIDChild(pk = UUID.randomUUID(), fk = UUID.randomUUID(), col = "col123")
            assertEquals(
                actual = this.ser.insertRow(row, batch = it),
                expected = Query(
                    sql = "INSERT INTO 'schema'.'UUIDChild' ('pk', 'fk', 'col') VALUES (?, ?, ?)",
                    QueryValue(name = "pk", value = row.pk, jdbcType = JDBCType.CHAR, encoder = UUIDTS.sqlite.encoder),
                    QueryValue(name = "fk", value = row.fk, jdbcType = JDBCType.CHAR, encoder = UUIDTS.sqlite.encoder),
                    QueryValue(name = "col", value = row.col, jdbcType = JDBCType.VARCHAR, encoder = StringTS.string(100).encoder)
                )
            )
        }
    }

    @Test
    fun `test updateRow`() {
        assertEquals(
            actual = this.ser.updateRow(row = Parent(pk = 123, col = "col123")),
            expected = Query(
                sql = "UPDATE 'schema'.'Parent' SET 'col' = ? WHERE 'schema'.'Parent'.'pk' = ?",
                QueryValue(name = "col", value = "col123", jdbcType = JDBCType.VARCHAR, encoder = StringTS.string(100).encoder),
                QueryValue(name = "pk", value = 123, jdbcType = JDBCType.INTEGER, encoder = NumberTS.int.encoder),
            )
        )
        assertEquals(
            actual = this.ser.updateRow(row = Child(pk = 123, fk = 321, col = "col123")),
            expected = Query(
                sql = "UPDATE 'schema'.'Child' SET 'fk' = ?, 'col' = ? WHERE 'schema'.'Child'.'pk' = ?",
                QueryValue(name = "fk", value = 321, jdbcType = JDBCType.INTEGER, encoder = NumberTS.int.encoder),
                QueryValue(name = "col", value = "col123", jdbcType = JDBCType.VARCHAR, encoder = StringTS.string(100).encoder),
                QueryValue(name = "pk", value = 123, jdbcType = JDBCType.INTEGER, encoder = NumberTS.int.encoder),
            )
        )
    }

    @Test
    fun `test selectTable`() {
        assertEquals(
            actual = this.ser.selectTable(table = Parent::class),
            expected = Query(sql = "SELECT * FROM 'schema'.'Parent'")
        )
    }

    @Test
    fun `test selectTable page`() {
        assertEquals(
            actual = this.ser.selectTable(table = Parent::class, page = Page(number = 3, orderBy = Parent::pk, limit = 15, order = Order.DESC)),
            expected = Query(sql = "SELECT * FROM 'schema'.'Parent' ORDER BY 'pk' DESC LIMIT 15 OFFSET 45")
        )
    }

    @Test
    fun `test selectTable cursor`() {
        assertEquals(
            actual = this.ser.selectTable(table = Parent::class, cursor = Cursor(index = 123, orderBy = Parent::pk, limit = 15, order = Order.DESC)),
            expected = Query(sql = "SELECT * FROM 'schema'.'Parent' WHERE 'pk' <= 123 ORDER BY 'pk' DESC LIMIT 15")
        )
        assertEquals(
            actual = this.ser.selectTable(table = Parent::class, cursor = Cursor(index = 123, orderBy = Parent::pk, limit = 15, order = Order.ASC)),
            expected = Query(sql = "SELECT * FROM 'schema'.'Parent' WHERE 'pk' >= 123 ORDER BY 'pk' ASC LIMIT 15")
        )
    }

    @Test
    fun `test selectTable pk`() {
        assertEquals(
            actual = this.ser.selectTable(table = Parent::class, pk = 123),
            expected = Query(sql = "SELECT * FROM 'schema'.'Parent' WHERE 'schema'.'Parent'.'pk' = 123")
        )
        val uuid = UUID.randomUUID()
        assertEquals(
            actual = this.ser.selectTable(table = Parent::class, pk = uuid),
            expected = Query(sql = "SELECT * FROM 'schema'.'Parent' WHERE 'schema'.'Parent'.'pk' = '$uuid'")
        )
    }

    @Test
    fun `test query`() {
        assertEquals(
            actual = this.ser.query {
                listOf(
                    it.table<Parent>(),
                    it.name(Parent::pk),
                    it.procedure<TestProcedure>(),
                    it.DELETE<Parent>(),
                    it.SELECT<Parent>(),
                ).joinToString()
            },
            expected = Query(
                sql = listOf(
                    "'schema'.'Parent'",
                    "'pk'",
                    "'TestProcedure'",
                    "DELETE FROM 'schema'.'Parent'",
                    "SELECT * FROM 'schema'.'Parent'"
                ).joinToString()
            )
        )
    }

    @Test
    fun `test queryWithInput`() {
        assertEquals(
            actual = this.ser.queryWithInput(
                input = Child(pk = 123, fk = 321, col = "col123")
            ) {
                listOf(
                    it.input(Child::fk),
                    it.table<Parent>(),
                    it.name(Parent::pk),
                    it.procedure<TestProcedure>(),
                    it.DELETE<Parent>(),
                    it.SELECT<Parent>(),
                ).joinToString()
            },
            expected = Query(
                sql = listOf(
                    "?",
                    "'schema'.'Parent'",
                    "'pk'",
                    "'TestProcedure'",
                    "DELETE FROM 'schema'.'Parent'",
                    "SELECT * FROM 'schema'.'Parent'"
                ).joinToString(),
                QueryValue(name = "fk", value = 321, jdbcType = JDBCType.INTEGER, encoder = NumberTS.int.encoder),
            )
        )
    }
}
