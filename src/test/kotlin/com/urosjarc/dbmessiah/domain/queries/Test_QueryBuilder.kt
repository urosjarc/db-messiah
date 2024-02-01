package com.urosjarc.dbmessiah.domain.queries

import com.urosjarc.dbmessiah.DbMessiahMapper
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
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class Test_QueryBuilder {
    private data class Parent(
        var pk: Int? = null,
    )
    private data class Child(
        var pk: Int? = null,
        var fk: Int? = null,
    )
    private data class Input(var id: Int, val property: String)
    private data class Output(var text: String?)
    private val testSchema = Schema(
        name = "main", tables = listOf(
            Table(primaryKey = Parent::pk),
            Table(
                primaryKey = Child::pk, foreignKeys = listOf(
                    Child::fk to Parent::class
                )
            )
        )
    )

    private lateinit var queryBuilderOut: QueryBuilderOut<Output>
    private lateinit var input: Input
    private lateinit var queryBuilderInOut: QueryBuilderInOut<Input, Output>
    private lateinit var escaper: Escaper

    @BeforeEach
    fun init_table() {
        this.input = Input(id = 123, property = "property1")
        this.escaper = Escaper(type = Escaper.Type.SINGLE_QUOTES, joinStr = ".")
        this.queryBuilderInOut = QueryBuilderInOut(
            output = Output::class,
            input = input,
            mapper = DbMessiahMapper(
                escaper = escaper,
                schemas = listOf(testSchema), globalSerializers = AllTS.basic,
                globalOutputs = listOf(Output::class), globalInputs = listOf(Input::class)
            )
        )
        this.queryBuilderOut = QueryBuilderOut(
            output = Output::class,
            mapper = DbMessiahMapper(
                escaper = escaper,
                schemas = listOf(testSchema), globalSerializers = AllTS.basic,
                globalOutputs = listOf(Output::class), globalInputs = listOf(Input::class)
            )
        )
    }

    @Test
    fun `test init {}`() {
        val e0 = assertThrows<SerializerException> {
            QueryBuilderInOut(
                output = Output::class,
                input = input,
                mapper = DbMessiahMapper(
                    escaper = escaper,
                    schemas = listOf(testSchema), globalSerializers = AllTS.basic,
                    globalOutputs = listOf(Output::class), globalInputs = listOf()
                )
            )
        }
        assertContains(charSequence = e0.message.toString(), other = "not registered in serializers global inputs")

        val e1 = assertThrows<SerializerException> {
            QueryBuilderInOut(
                output = Output::class,
                input = input,
                mapper = DbMessiahMapper(
                    escaper = escaper,
                    schemas = listOf(testSchema), globalSerializers = AllTS.basic,
                    globalOutputs = listOf(), globalInputs = listOf(Input::class)
                )
            )
        }
        assertContains(charSequence = e1.message.toString(), other = "not registered in serializers global outputs")

        val e2 = assertThrows<SerializerException> {
            QueryBuilderOut(
                output = Output::class,
                mapper = DbMessiahMapper(
                    escaper = escaper,
                    schemas = listOf(testSchema), globalSerializers = AllTS.basic,
                    globalOutputs = listOf(), globalInputs = listOf()
                )
            )
        }
        assertContains(charSequence = e2.message.toString(), other = "not registered in serializers global outputs")
    }

    @Test
    fun `test inp(), build()`() {
        assertEquals(expected = "?", actual = queryBuilderInOut.inp(Input::id))
        assertEquals(expected = "?", actual = queryBuilderInOut.inp(Input::property))
        val sql = "SELECT * FROM Input"
        val actual: Query = queryBuilderInOut.build(sql = sql)
        val expected = Query(
            sql = sql,
            QueryValue(name = "id", value = input.id, jdbcType = JDBCType.INTEGER, encoder = NumberTS.Int.encoder),
            QueryValue(name = "property", value = input.property, jdbcType = JDBCType.VARCHAR, encoder = StringTS.String(0).encoder)
        )
        assertEquals(actual = actual.sql, expected = expected.sql)

        assertEquals(expected = 2, actual = expected.values.size)
        actual.values.forEachIndexed { index, actValue ->
            assertEquals(actual = actValue, expected = expected.values[index])
        }
    }

    @Test
    fun `test out()`() {
        assertEquals(expected = "'text'", actual = this.queryBuilderOut.out(Output::text))
    }
}
