package com.urosjarc.dbmessiah.impl

import com.urosjarc.dbmessiah.Test_Serializer
import com.urosjarc.dbmessiah.data.Query
import com.urosjarc.dbmessiah.data.QueryValue
import com.urosjarc.dbmessiah.domain.C
import com.urosjarc.dbmessiah.domain.Order
import com.urosjarc.dbmessiah.domain.Page
import com.urosjarc.dbmessiah.domain.Table
import com.urosjarc.dbmessiah.impl.mssql.MssqlSchema
import com.urosjarc.dbmessiah.impl.mssql.MssqlSerializer
import com.urosjarc.dbmessiah.serializers.AllTS
import com.urosjarc.dbmessiah.serializers.NumberTS
import com.urosjarc.dbmessiah.serializers.StringTS
import com.urosjarc.dbmessiah.serializers.UUIDTS
import org.junit.jupiter.api.BeforeEach
import java.sql.JDBCType
import java.util.*
import kotlin.test.Test
import kotlin.test.assertEquals

class Test_MssqlSerializer : Test_Serializer() {
    override fun wrap(name: String): String = "[$name]"

    @BeforeEach
    override fun init() {
        super.init()
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
        this.schema = MssqlSchema(name = "main", tables = this.tables + this.otherTables + this.tablesNonAutoUUID)
        this.ser = MssqlSerializer(
            schemas = listOf(this.schema as MssqlSchema),
            globalProcedures = listOf(TestProcedure::class, TestProcedureEmpty::class),
            globalSerializers = AllTS.sqlite,
        )
    }

    @Test
    override fun `test dropTable`() {
        assertEquals(
            actual = this.ser.dropTable(table = Parent::class),
            expected = Query(sql = "DROP TABLE IF EXISTS ${wrap("main")}.${wrap("Parent")}")
        )
        assertEquals(
            actual = this.ser.dropTable(table = Parent::class, cascade = true),
            expected = Query(sql = "DROP TABLE IF EXISTS ${wrap("main")}.${wrap("Parent")}")
        )
    }

    @Test
    override fun `test escaped procedureArg`() {
        assertEquals(actual = this.ser.escaped(procedureArg = TestProcedure::parent_pk), expected = "@parent_pk")
    }
    @Test
    override fun `test selectTable page`() {
        assertEquals(
            actual = this.ser.selectTable(table = Parent::class, page = Page(number = 3, orderBy = Parent::pk, limit = 15, order = Order.DESC)),
            expected = Query(sql = "SELECT * FROM ${wrap("main")}.${wrap("Parent")} ORDER BY ${wrap("pk")} OFFSET 45 ROWS FETCH NEXT 15 ROWS ONLY")
        )
    }
    @Test
    override fun `test plantUML`() {
        assertEquals(
            actual = this.ser.plantUML(),
            expected = listOf(
                "@startuml",
                "skinparam backgroundColor darkgray",
                "skinparam ClassBackgroundColor lightgray",
                "",
                "package main <<Folder>> {",
                "\t class main.Parent {",
                "\t\t pk: Int",
                "\t }",
                "\t class main.Child {",
                "\t\t pk: Int",
                "\t\t fk: Parent",
                "\t }",
                "\t class main.Other {",
                "\t\t pk: Int",
                "\t\t cascades: Other",
                "\t }",
                "\t class main.UUIDChild {",
                "\t\t pk: UUID",
                "\t }",
                "}",
                "",
                "main.Child -down-> main.Parent: fk",
                "main.Other -down-> main.Other: cascades",
                "",
                "@enduml"
            ).joinToString("\n")
        )
    }

    @Test
    override fun `test escaped name`() {
        assertEquals(expected = "[name]", actual = ser.escaped("name"))
    }

    @Test
    override fun `test createTable`() {
        assertEquals(
            expected = this.ser.createTable(Parent::class),
            actual = Query(sql = """CREATE TABLE [main].[Parent] ([pk] INTEGER PRIMARY KEY IDENTITY(1,1), [col] VARCHAR(100) NOT NULL)"""),
        )
        assertEquals(
            expected = this.ser.createTable(Child::class),
            actual = Query(sql = """CREATE TABLE [main].[Child] ([pk] INTEGER PRIMARY KEY, [fk] INTEGER NOT NULL, [col] VARCHAR(100) NOT NULL, FOREIGN KEY ([fk]) REFERENCES [main].[Parent] ([pk]))"""),
        )
        assertEquals(
            expected = this.ser.createTable(Other::class),
            actual = Query(sql = """CREATE TABLE [main].[Other] ([pk] INTEGER PRIMARY KEY, [cascades] INTEGER NOT NULL, [canBeNull] INTEGER, [notNull] INTEGER NOT NULL, [unique] INTEGER NOT NULL UNIQUE, FOREIGN KEY ([cascades]) REFERENCES [main].[Other] ([pk]) ON UPDATE CASCADE ON DELETE CASCADE)"""),
        )
    }

    @Test
    override fun `test dropSchema`() {
        assertEquals(
            actual = this.ser.dropSchema(schema = this.schema),
            expected = Query(sql = "DROP SCHEMA IF EXISTS [main]")
        )
        assertEquals(
            actual = this.ser.dropSchema(schema = this.schema, cascade = true),
            expected = Query(sql = "DROP SCHEMA IF EXISTS [main] CASCADE")
        )
    }

    @Test
    override fun `test createSchema`() {
        assertEquals(
            actual = this.ser.createSchema(schema = this.schema),
            expected = Query(sql = "IF NOT EXISTS (SELECT 1 FROM sys.schemas WHERE name = 'main')\nBEGIN\n    EXEC( 'CREATE SCHEMA [main]' );\nEND")
        )
    }

    @Test
    override fun `test insertRow auto UUID incremental`() = Unit

    @Test
    override fun `test insertRow non auto uuid incremental`() {
        listOf(false, true).forEach {
            val row = UUIDChild(pk = UUID.randomUUID(), fk = UUID.randomUUID(), col = "col123")
            assertEquals(
                actual = this.ser.insertRow(row, batch = it),
                expected = Query(
                    sql = "INSERT INTO ${wrap("main")}.${wrap("UUIDChild")} (${wrap("pk")}, ${wrap("col")}, ${wrap("fk")}) VALUES (?, ?, ?)",
                    QueryValue(name = "pk", value = row.pk, jdbcType = JDBCType.CHAR, encoder = UUIDTS.sqlite.encoder),
                    QueryValue(name = "col", value = row.col, jdbcType = JDBCType.VARCHAR, encoder = StringTS.string(100).encoder),
                    QueryValue(name = "fk", value = row.fk, jdbcType = JDBCType.CHAR, encoder = UUIDTS.sqlite.encoder),
                )
            )
        }
    }

    @Test
    override fun `test createProcedure`() {
        val proc0 = this.ser.createProcedure(procedure = TestProcedure::class, procedureBody = "BODY")
        val proc1 = this.ser.createProcedure(procedure = TestProcedureEmpty::class, procedureBody = "BODY")
        assertEquals(
            expected = Query(sql = "CREATE OR ALTER PROCEDURE [TestProcedure] @parent_col VARCHAR(100), @parent_pk INTEGER\nBEGIN\n    BODY\nEND"),
            actual = proc0
        )
        assertEquals(
            expected = Query(sql = "CREATE OR ALTER PROCEDURE [TestProcedureEmpty] \nBEGIN\n    BODY\nEND"),
            actual = proc1
        )
    }

    @Test
    override fun `test callProcedure`() {
        assertEquals(
            actual = this.ser.callProcedure(procedure = TestProcedure(parent_pk = 123, parent_col = "col123")),
            expected = Query(
                sql = "EXEC [TestProcedure] @parent_col = ?, @parent_pk = ?",
                QueryValue(name = "parent_col", value = "col123", jdbcType = JDBCType.VARCHAR, encoder = StringTS.string(100).encoder),
                QueryValue(name = "parent_pk", value = 123, jdbcType = JDBCType.INTEGER, encoder = NumberTS.int.encoder)
            )
        )
        assertEquals(
            actual = this.ser.callProcedure(procedure = TestProcedureEmpty()),
            expected = Query(sql = "EXEC [TestProcedureEmpty] "),
        )
    }

    @Test
    override fun `test dropProcedure`() {
        assertEquals(
            actual = this.ser.dropProcedure(procedure = TestProcedureEmpty::class),
            expected = Query(sql = "DROP PROCEDURE IF EXISTS [TestProcedureEmpty]"),
        )
        assertEquals(
            actual = this.ser.dropProcedure(procedure = TestProcedure::class),
            expected = Query(sql = "DROP PROCEDURE IF EXISTS [TestProcedure]"),
        )
    }
}
