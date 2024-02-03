package com.urosjarc.dbmessiah

import com.urosjarc.dbmessiah.domain.queries.Page
import com.urosjarc.dbmessiah.domain.queries.Query
import com.urosjarc.dbmessiah.domain.queries.QueryValue
import com.urosjarc.dbmessiah.domain.schema.Schema
import com.urosjarc.dbmessiah.domain.table.Escaper
import com.urosjarc.dbmessiah.domain.table.Table
import com.urosjarc.dbmessiah.exceptions.SerializerException
import com.urosjarc.dbmessiah.types.AllTS
import com.urosjarc.dbmessiah.types.NumberTS
import com.urosjarc.dbmessiah.types.StringTS
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.assertThrows
import java.sql.JDBCType
import kotlin.reflect.KClass
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals

class Test_DbMessiahSerializer {

    private lateinit var seri: TestSerializer

    data class Parent(
        var pk: Int? = null,
        val col: String = "12"
    )

    private data class Child(
        var pk: Int? = null,
        var fk: Int? = null,
    )

    private data class Input(val parent_search: String, val child_search: String)
    private data class Output(val child_id: Int, val parent_id: Int)

    private class TestSerializer : DbMessiahSerializer(
        schemas = listOf(
            Schema(
                name = "main", tables = listOf(
                    Table(primaryKey = Parent::pk),
                    Table(
                        primaryKey = Child::pk, foreignKeys = listOf(
                            Child::fk to Parent::class
                        )
                    )
                )
            )
        ),
        globalSerializers = AllTS.basic,
        globalInputs = listOf(Input::class),
        globalOutputs = listOf(Output::class),
        globalProcedures = listOf(),
        escaper = Escaper()
    ) {
        override val onGeneratedKeysFail: String get() = TODO("Not yet implemented")

        override fun <T : Any> dropQuery(kclass: KClass<T>): Query = TODO("Not yet implemented")
        override fun <T : Any> createQuery(kclass: KClass<T>): Query = TODO("Not yet implemented")
        override fun <T : Any> deleteQuery(kclass: KClass<T>): Query = TODO("Not yet implemented")
        override fun deleteQuery(obj: Any): Query = TODO("Not yet implemented")
        override fun insertQuery(obj: Any): Query = TODO("Not yet implemented")
        override fun updateQuery(obj: Any): Query = TODO("Not yet implemented")
        override fun <T : Any> query(kclass: KClass<T>): Query = TODO("Not yet implemented")
        override fun <T : Any> query(kclass: KClass<T>, page: Page<T>): Query = TODO("Not yet implemented")
        override fun <T : Any, K : Any> query(kclass: KClass<T>, pk: K): Query = TODO("Not yet implemented")
        override fun <T : Any> callQuery(obj: T): Query = TODO("Not yet implemented")
    }

    @BeforeEach
    fun init() {
        this.seri = TestSerializer()
    }

    @Test
    fun `test query(0)`() {
        val query = this.seri.query { "SELECT * FROM xxx" }
        assertEquals(expected = 0, actual = query.values.size)
        assertEquals(actual = query.sql, expected = "SELECT * FROM xxx")

        val e = assertThrows<SerializerException> {
            this.seri.query(input = "asdf") { " ${it.get(String::length)} " }
        }
        assertContains(
            charSequence = e.message.toString(),
            other = "Input class 'String' is not registered in global inputs",
            message = e.toString()
        )
    }

    @Test
    fun `test selectQuery(1)`() {
        val input = Input(parent_search = "parent", child_search = "child")
        val query = this.seri.query(input = input) {
            """
                SELECT
                    col0 as ${Output::child_id.name}
                    col1 as ${Output::parent_id.name}
                FROM xxx WHERE ${it.get(Input::parent_search)} > col0 AND ${it.get(Input::child_search)} < col1 OR ${it.get(Input::child_search)}
            """.trimIndent()
        }
        assertEquals(expected = 3, actual = query.values.size)
        assertEquals(
            actual = query.sql, expected = "SELECT\n" +
                    "    col0 as child_id\n" +
                    "    col1 as parent_id\n" +
                    "FROM xxx WHERE ? > col0 AND ? < col1 OR ?"
        )
        assertEquals(
            actual = query.values[0],
            expected = QueryValue(name = "parent_search", value = "parent", jdbcType = JDBCType.VARCHAR, encoder = StringTS.String(0).encoder)
        )
        assertEquals(
            actual = query.values[1],
            expected = QueryValue(name = "child_search", value = "child", jdbcType = JDBCType.VARCHAR, encoder = StringTS.String(0).encoder)
        )
        assertEquals(
            actual = query.values[2],
            expected = QueryValue(name = "child_search", value = "child", jdbcType = JDBCType.VARCHAR, encoder = StringTS.String(0).encoder)
        )

        val e = assertThrows<SerializerException> {
            this.seri.query(input = "asdf") { " ${it.get(String::length)} " }
        }
        assertContains(
            charSequence = e.message.toString(),
            other = "Input class 'String' is not registered in global inputs",
            message = e.toString()
        )
    }
}
