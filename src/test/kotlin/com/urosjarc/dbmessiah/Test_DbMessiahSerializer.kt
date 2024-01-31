package com.urosjarc.dbmessiah

import NumberTS
import com.urosjarc.dbmessiah.domain.queries.Page
import com.urosjarc.dbmessiah.domain.queries.Query
import com.urosjarc.dbmessiah.domain.queries.QueryValue
import com.urosjarc.dbmessiah.domain.test.TestInput
import com.urosjarc.dbmessiah.domain.test.TestOutput
import com.urosjarc.dbmessiah.domain.test.TestTableParent
import com.urosjarc.dbmessiah.exceptions.SerializerException
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
    class TestSerializer : DbMessiahSerializer(schemas = listOf(), globalSerializers = listOf(), globalInputs = listOf(), globalOutputs = listOf(), injectTestElements = true) {
        override val onGeneratedKeysFail: String
            get() = TODO("Not yet implemented")

        override fun <T : Any> dropQuery(kclass: KClass<T>): Query {
            TODO("Not yet implemented")
        }

        override fun <T : Any> createQuery(kclass: KClass<T>): Query {
            TODO("Not yet implemented")
        }

        override fun <T : Any> deleteQuery(kclass: KClass<T>): Query {
            TODO("Not yet implemented")
        }

        override fun deleteQuery(obj: Any): Query {
            TODO("Not yet implemented")
        }

        override fun insertQuery(obj: Any): Query {
            TODO("Not yet implemented")
        }

        override fun updateQuery(obj: Any): Query {
            TODO("Not yet implemented")
        }

        override fun <T : Any> selectQuery(kclass: KClass<T>): Query {
            TODO("Not yet implemented")
        }

        override fun <T : Any> selectQuery(kclass: KClass<T>, page: Page<T>): Query {
            TODO("Not yet implemented")
        }

        override fun <T : Any, K : Any> selectQuery(kclass: KClass<T>, pk: K): Query {
            TODO("Not yet implemented")
        }

    }

    private lateinit var seri: TestSerializer

    data class Child(var pk: Int, val fk: String, val col: Float)
    data class Parent(var pk: Int, val col: String)

    @BeforeEach
    fun init() {
        this.seri = TestSerializer()
    }


    @Test
    fun `test callQuery()`() {
        val input = TestTableParent(id = 12, col13 = "123")
        val query = this.seri.callQuery(input = input)
        assertEquals(actual = query.sql, expected = "{CALL TestTableParent(?)}")
        assertEquals(expected = 1, actual = query.values.size)
        assertEquals(actual = query.values[0], expected = QueryValue(name = "\"col13\"", value = input.col13, jdbcType = JDBCType.VARCHAR, encoder = NumberTS.Int.encoder))

        val e = assertThrows<SerializerException> {
            this.seri.callQuery(input = "asdfasdf")
        }
        assertContains(charSequence = e.message.toString(), other = "Could not find table info for table 'String'", message = e.toString())
    }

    @Test
    fun `test selectQuery()`() {
        val query = this.seri.selectQuery(output = TestOutput::class) {
            """
                SELECT
                    col0 as ${it.out(TestOutput::child_id)}
                    col1 as ${it.out(TestOutput::parent_id)}
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
    fun `test selectQuery()`() {
        val query = this.seri.selectQuery(input = TestInput(parent_search = "parent", child_search = "child"), output = TestOutput::class) {
            """
                SELECT
                    col0 as ${it.out(TestOutput::child_id)}
                    col1 as ${it.out(TestOutput::parent_id)}
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
}
