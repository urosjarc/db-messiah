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
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows
import java.sql.JDBCType
import kotlin.reflect.KClass
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
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
        escaper = Escaper()
    ) {
        override val onGeneratedKeysFail: String get() = TODO("Not yet implemented")
        override fun <T : Any> dropQuery(kclass: KClass<T>): Query = TODO("Not yet implemented")
        override fun <T : Any> createQuery(kclass: KClass<T>): Query = TODO("Not yet implemented")
        override fun <T : Any> deleteQuery(kclass: KClass<T>): Query = TODO("Not yet implemented")
        override fun deleteQuery(obj: Any): Query = TODO("Not yet implemented")
        override fun insertQuery(obj: Any): Query = TODO("Not yet implemented")
        override fun updateQuery(obj: Any): Query = TODO("Not yet implemented")
        override fun <T : Any> selectQuery(kclass: KClass<T>): Query = TODO("Not yet implemented")
        override fun <T : Any> selectQuery(kclass: KClass<T>, page: Page<T>): Query = TODO("Not yet implemented")
        override fun <T : Any, K : Any> selectQuery(kclass: KClass<T>, pk: K): Query = TODO("Not yet implemented")

    }

    @BeforeEach
    fun init() {
        this.seri = TestSerializer()
    }


    @Test
    fun `test callQuery()`() {
        val input = Parent(pk = 12, col = "123")
        val query = this.seri.callQuery(input = input)
        assertEquals(actual = query.sql, expected = "{CALL Parent(?)}")
        assertEquals(expected = 1, actual = query.values.size)
        assertEquals(actual = query.values[0], expected = QueryValue(name = "\"col\"", value = input.col, jdbcType = JDBCType.VARCHAR, encoder = NumberTS.Int.encoder))

        val e = assertThrows<SerializerException> {
            this.seri.callQuery(input = "asdfasdf")
        }
        assertContains(charSequence = e.message.toString(), other = "Could not find table info for table 'String'", message = e.toString())
    }

    @Test
    fun `test selectQuery(0)`() {
        val query = this.seri.selectQuery(output = Output::class) { "SELECT * FROM xxx" }
        assertEquals(expected = 0, actual = query.values.size)
        assertEquals(actual = query.sql, expected = "SELECT * FROM xxx")

        val e = assertThrows<SerializerException> {
            this.seri.selectQuery(output = String::class) { " ${it.out(String::length)} " }
        }
        assertContains(charSequence = e.message.toString(), other = " Output class 'String' is not registered in serializers global outputs!", message = e.toString())
    }

    @Test
    fun `test selectQuery(1)`() {
        val query = this.seri.selectQuery(output = Output::class) {
            """
                SELECT
                    col0 as ${it.out(Output::child_id)}
                    col1 as ${it.out(Output::parent_id)}
                FROM xxx
            """.trimIndent()
        }
        assertEquals(expected = 0, actual = query.values.size)
        assertEquals(
            actual = query.sql, expected = "SELECT\n" +
                    "    col0 as \"child_id\"\n" +
                    "    col1 as \"parent_id\"\n" +
                    "FROM xxx"
        )

        val e = assertThrows<SerializerException> {
            this.seri.selectQuery(output = String::class) { " ${it.out(String::length)} " }
        }
        assertContains(charSequence = e.message.toString(), other = " Output class 'String' is not registered in serializers global outputs!", message = e.toString())
    }

    @Test
    fun `test selectQuery(2)`() {
        val input = Input(parent_search = "parent", child_search = "child")
        val query = this.seri.selectQuery(input = input, output = Output::class) {
            """
                SELECT
                    col0 as ${it.out(Output::child_id)}
                    col1 as ${it.out(Output::parent_id)}
                FROM xxx WHERE ${it.inp(Input::parent_search)} > col0 AND ${it.inp(Input::child_search)} < col1 OR ${it.inp(Input::child_search)}
            """.trimIndent()
        }
        assertEquals(expected = 3, actual = query.values.size)
        assertEquals(
            actual = query.sql, expected = "SELECT\n" +
                    "    col0 as \"child_id\"\n" +
                    "    col1 as \"parent_id\"\n" +
                    "FROM xxx WHERE ? > col0 AND ? < col1 OR ?"
        )
        assertEquals(actual = query.values[0], expected = QueryValue(name = "parent_search", value = "parent", jdbcType = JDBCType.VARCHAR, encoder = StringTS.String(0).encoder))
        assertEquals(actual = query.values[1], expected = QueryValue(name = "child_search", value = "child", jdbcType = JDBCType.VARCHAR, encoder = StringTS.String(0).encoder))
        assertEquals(actual = query.values[2], expected = QueryValue(name = "child_search", value = "child", jdbcType = JDBCType.VARCHAR, encoder = StringTS.String(0).encoder))

        val e = assertThrows<SerializerException> {
            this.seri.selectQuery(output = String::class, input = input) { " ${it.out(String::length)} " }
        }
        assertContains(charSequence = e.message.toString(), other = " Output class 'String' is not registered in serializers global outputs!", message = e.toString())
    }
}
