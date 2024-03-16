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

abstract class Test_Serializer {
    open var testProcedures: Boolean = true

    lateinit var otherTables: List<Table<Other>>
    lateinit var tablesNonAutoUUID: List<Table<UUIDChild>>
    lateinit var tables: List<Table<*>>
    lateinit var tablesUUID: List<Table<*>>
    lateinit var column: PrimaryColumn
    lateinit var tableInfo: TableInfo
    lateinit var procedureArg: ProcedureArg
    lateinit var procedure: Procedure
    lateinit var schema: Schema
    lateinit var ser: Serializer

    data class Other(val pk: Int, val notNull: Int, val unique: Int, val canBeNull: Int?, val cascades: Int)
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
        override fun escaped(name: String): String = "'$name'"
        override fun <T : Any> createTable(table: KClass<T>): Query = TODO("Not yet implemented")
        override fun <T : Any> createProcedure(procedure: KClass<T>, procedureBody: String): Query = TODO("Not yet implemented")
        override fun <T : Any> callProcedure(procedure: T): Query = TODO("Not yet implemented")
        override fun <T : Any> dropProcedure(procedure: KClass<T>): Query = TODO("Not yet implemented")
    }

    abstract fun wrap(name: String): String

    @BeforeEach
    open fun init() {
        this.column = PrimaryColumn(
            kprop = Parent::pk as KProperty1<Any, Any?>,
            dbType = "INTEGER",
            jdbcType = JDBCType.INTEGER,
            encoder = NumberTS.int.encoder,
            decoder = NumberTS.int.decoder
        )
        this.tableInfo = TableInfo(
            schema = "main",
            primaryColumn = this.column,
            kclass = Parent::class,
            foreignColumns = listOf(),
            otherColumns = listOf(),
            typeSerializers = listOf()
        )
        this.tables = listOf(
            Table(Parent::pk),
            Table(
                Child::pk, foreignKeys = listOf(
                    Child::fk to Parent::class
                )
            ),
        )
        this.otherTables = listOf(
            Table(
                Other::pk, foreignKeys = listOf(
                    Other::cascades to Other::class
                ), constraints = listOf(
                    Other::unique to listOf(C.UNIQUE),
                    Other::cascades to listOf(C.CASCADE_DELETE, C.CASCADE_UPDATE)
                )
            )
        )
        this.tablesNonAutoUUID = listOf(Table(UUIDChild::pk))
        this.tablesUUID = listOf(
            Table(UUIDParent::pk),
            Table(
                UUIDChild::pk,
                foreignKeys = listOf(UUIDChild::fk to UUIDParent::class),
                constraints = listOf(
                    UUIDChild::fk to listOf(C.CASCADE_DELETE)
                )
            )
        )
        this.schema = Schema(
            name = "main",
            tables = this.tables,
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
    abstract fun `test escaped name`()

    @Test
    fun `test escaped schema`() {
        assertEquals(actual = this.ser.escaped(schema = this.schema), expected = wrap("main"))
    }

    @Test
    fun `test escaped procedure`() {
        assertEquals(actual = this.ser.escaped(procedure = this.procedure), expected = "${wrap("main")}.${wrap("TestProcedure")}")
    }

    @Test
    open fun `test escaped procedureArg`() {
        assertEquals(actual = this.ser.escaped(procedureArg = TestProcedure::parent_pk), expected = wrap("parent_pk"))
    }

    @Test
    fun `test escaped tableInfo`() {
        assertEquals(actual = this.ser.escaped(tableInfo = this.tableInfo), expected = "${wrap("main")}.${wrap("Parent")}")
    }

    @Test
    fun `test escaped column`() {
        assertEquals(actual = this.ser.escaped(column = this.column), expected = "${wrap("main")}.${wrap("Parent")}.${wrap("pk")}")
    }

    @Test
    open fun `test plantUML`() {
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
    open fun `test createSchema`() {
        assertEquals(
            actual = this.ser.createSchema(schema = this.schema),
            expected = Query(sql = "CREATE SCHEMA IF NOT EXISTS ${wrap("main")}")
        )
    }

    @Test
    open fun `test dropSchema`() {
        assertEquals(
            actual = this.ser.dropSchema(schema = this.schema),
            expected = Query(sql = "DROP SCHEMA IF EXISTS ${wrap("main")}")
        )
        assertEquals(
            actual = this.ser.dropSchema(schema = this.schema, cascade = true),
            expected = Query(sql = "DROP SCHEMA IF EXISTS ${wrap("main")} CASCADE")
        )
    }

    @Test
    open fun `test dropTable`() {
        assertEquals(
            actual = this.ser.dropTable(table = Parent::class),
            expected = Query(sql = "DROP TABLE IF EXISTS ${wrap("main")}.${wrap("Parent")}")
        )
        assertEquals(
            actual = this.ser.dropTable(table = Parent::class, cascade = true),
            expected = Query(sql = "DROP TABLE IF EXISTS ${wrap("main")}.${wrap("Parent")} CASCADE")
        )
    }

    @Test
    abstract fun `test createTable`()

    @Test
    fun `test deleteTable`() {
        assertEquals(
            actual = this.ser.deleteTable(table = Parent::class),
            expected = Query(sql = "DELETE FROM ${wrap("main")}.${wrap("Parent")}")
        )
    }

    @Test
    fun `test deleteRow`() {
        assertEquals(
            actual = this.ser.deleteRow(row = Parent(pk = 123, col = "col123")),
            expected = Query(
                sql = "DELETE FROM ${wrap("main")}.${wrap("Parent")} WHERE ${wrap("main")}.${wrap("Parent")}.${wrap("pk")} = ?",
                QueryValue(name = "pk", value = 123, jdbcType = JDBCType.INTEGER, encoder = NumberTS.int.encoder)
            )
        )
    }

    @Test
    open fun `test insertRow auto int incremental`() {
        listOf(false, true).forEach {
            assertEquals(
                actual = this.ser.insertRow(row = Parent(col = "col123"), batch = it),
                expected = Query(
                    sql = "INSERT INTO ${wrap("main")}.${wrap("Parent")} (${wrap("col")}) VALUES (?)",
                    QueryValue(name = "col", value = "col123", jdbcType = JDBCType.VARCHAR, encoder = StringTS.string(100).encoder)
                )
            )
        }
    }

    @Test
    open fun `test insertRow auto UUID incremental`() {
        listOf(false, true).forEach {
            assertEquals(
                actual = this.ser.insertRow(row = UUIDParent(col = "col123"), batch = it),
                expected = Query(
                    sql = "INSERT INTO ${wrap("main")}.${wrap("UUIDParent")} (${wrap("col")}) VALUES (?)",
                    QueryValue(name = "col", value = "col123", jdbcType = JDBCType.VARCHAR, encoder = StringTS.string(100).encoder)
                )
            )
        }
    }

    @Test
    fun `test insertRow non auto int incremental`() {
        listOf(false, true).forEach {
            assertEquals(
                actual = this.ser.insertRow(row = Child(pk = 123, fk = 12, col = "col123"), batch = it),
                expected = Query(
                    sql = "INSERT INTO ${wrap("main")}.${wrap("Child")} (${wrap("pk")}, ${wrap("fk")}, ${wrap("col")}) VALUES (?, ?, ?)",
                    QueryValue(name = "pk", value = 123, jdbcType = JDBCType.INTEGER, encoder = NumberTS.int.encoder),
                    QueryValue(name = "fk", value = 12, jdbcType = JDBCType.INTEGER, encoder = NumberTS.int.encoder),
                    QueryValue(name = "col", value = "col123", jdbcType = JDBCType.VARCHAR, encoder = StringTS.string(100).encoder)
                )
            )
        }
    }

    @Test
    open fun `test insertRow non auto uuid incremental`() {
        listOf(false, true).forEach {
            val row = UUIDChild(pk = UUID.randomUUID(), fk = UUID.randomUUID(), col = "col123")
            assertEquals(
                actual = this.ser.insertRow(row, batch = it),
                expected = Query(
                    sql = "INSERT INTO ${wrap("main")}.${wrap("UUIDChild")} (${wrap("pk")}, ${wrap("fk")}, ${wrap("col")}) VALUES (?, ?, ?)",
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
                sql = "UPDATE ${wrap("main")}.${wrap("Parent")} SET ${wrap("col")} = ? WHERE ${wrap("main")}.${wrap("Parent")}.${
                    wrap(
                        "pk"
                    )
                } = ?",
                QueryValue(name = "col", value = "col123", jdbcType = JDBCType.VARCHAR, encoder = StringTS.string(100).encoder),
                QueryValue(name = "pk", value = 123, jdbcType = JDBCType.INTEGER, encoder = NumberTS.int.encoder),
            )
        )
        assertEquals(
            actual = this.ser.updateRow(row = Child(pk = 123, fk = 321, col = "col123")),
            expected = Query(
                sql = "UPDATE ${wrap("main")}.${wrap("Child")} SET ${wrap("fk")} = ?, ${wrap("col")} = ? WHERE ${wrap("main")}.${wrap("Child")}.${
                    wrap(
                        "pk"
                    )
                } = ?",
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
            expected = Query(sql = "SELECT * FROM ${wrap("main")}.${wrap("Parent")}")
        )
    }

    @Test
    open fun `test selectTable page`() {
        assertEquals(
            actual = this.ser.selectTable(table = Parent::class, page = Page(number = 3, orderBy = Parent::pk, limit = 15, order = Order.DESC)),
            expected = Query(sql = "SELECT * FROM ${wrap("main")}.${wrap("Parent")} ORDER BY ${wrap("pk")} DESC LIMIT 15 OFFSET 45")
        )
    }

    @Test
    fun `test selectTable cursor`() {
        assertEquals(
            actual = this.ser.selectTable(table = Parent::class, cursor = Cursor(index = 123, orderBy = Parent::pk, limit = 15, order = Order.DESC)),
            expected = Query(sql = "SELECT * FROM ${wrap("main")}.${wrap("Parent")} WHERE ${wrap("pk")} <= 123 ORDER BY ${wrap("pk")} DESC LIMIT 15")
        )
        assertEquals(
            actual = this.ser.selectTable(table = Parent::class, cursor = Cursor(index = 123, orderBy = Parent::pk, limit = 15, order = Order.ASC)),
            expected = Query(sql = "SELECT * FROM ${wrap("main")}.${wrap("Parent")} WHERE ${wrap("pk")} >= 123 ORDER BY ${wrap("pk")} ASC LIMIT 15")
        )
    }

    @Test
    fun `test selectTable pk`() {
        assertEquals(
            actual = this.ser.selectTable(table = Parent::class, pk = 123),
            expected = Query(
                sql = "SELECT * FROM ${wrap("main")}.${wrap("Parent")} WHERE ${wrap("main")}.${wrap("Parent")}.${
                    wrap(
                        "pk"
                    )
                } = 123"
            )
        )
        val uuid = UUID.randomUUID()
        assertEquals(
            actual = this.ser.selectTable(table = Parent::class, pk = uuid),
            expected = Query(
                sql = "SELECT * FROM ${wrap("main")}.${wrap("Parent")} WHERE ${wrap("main")}.${wrap("Parent")}.${
                    wrap(
                        "pk"
                    )
                } = '$uuid'"
            )
        )
    }

    @Test
    fun `test query`() {
        assertEquals(
            actual = this.ser.query {
                listOf(
                    it.table<Parent>(),
                    it.name(Parent::pk),
                    if(this.testProcedures) it.procedure<TestProcedure>() else "",
                    it.DELETE<Parent>(),
                    it.SELECT<Parent>(),
                ).joinToString()
            },
            expected = Query(
                sql = listOf(
                    "${wrap("main")}.${wrap("Parent")}",
                    wrap("pk"),
                    if(this.testProcedures) wrap("TestProcedure") else "",
                    "DELETE FROM ${wrap("main")}.${wrap("Parent")}",
                    "SELECT * FROM ${wrap("main")}.${wrap("Parent")}"
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
                    if(this.testProcedures) it.procedure<TestProcedure>() else "",
                    it.DELETE<Parent>(),
                    it.SELECT<Parent>(),
                ).joinToString()
            },
            expected = Query(
                sql = listOf(
                    "?",
                    "${wrap("main")}.${wrap("Parent")}",
                    wrap("pk"),
                    if(this.testProcedures) wrap("TestProcedure") else "",
                    "DELETE FROM ${wrap("main")}.${wrap("Parent")}",
                    "SELECT * FROM ${wrap("main")}.${wrap("Parent")}"
                ).joinToString(),
                QueryValue(name = "fk", value = 321, jdbcType = JDBCType.INTEGER, encoder = NumberTS.int.encoder),
            )
        )
    }

    @Test
    abstract fun `test createProcedure`()

    @Test
    abstract fun `test callProcedure`()

    @Test
    abstract fun `test dropProcedure`()
}
