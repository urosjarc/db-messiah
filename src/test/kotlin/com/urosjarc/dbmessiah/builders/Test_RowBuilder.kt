package com.urosjarc.dbmessiah.builders

import com.urosjarc.dbmessiah.data.Column
import com.urosjarc.dbmessiah.data.QueryValue
import com.urosjarc.dbmessiah.serializers.NumberTS
import com.urosjarc.dbmessiah.serializers.StringTS
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.sql.JDBCType
import kotlin.reflect.KProperty1
import kotlin.test.assertEquals

class Test_RowBuilder {
    private lateinit var parent: Parent
    private lateinit var rb: RowBuilder

    data class Parent(var col0: Int?, val col1: String, val col2: Float)

    @BeforeEach
    fun init() {
        this.parent = Parent(col0 = 0, col1 = "1", col2 = 2.2f)
        this.rb = RowBuilder(
            columns = listOf(
                Column(
                    kprop = Parent::col0 as KProperty1<Any, Any?>,
                    dbType = "INTEGER",
                    jdbcType = JDBCType.INTEGER,
                    encoder = NumberTS.int.encoder,
                    decoder = NumberTS.int.decoder
                ),
                Column(
                    kprop = Parent::col1 as KProperty1<Any, Any?>,
                    dbType = "VARCHAR",
                    jdbcType = JDBCType.VARCHAR,
                    encoder = StringTS.string(100).encoder,
                    decoder = StringTS.string(100).decoder
                )
            )
        )
    }

    @Test
    fun `test sqlColumns`() {
        assertEquals(actual = this.rb.sqlColumns { "\"$it\"" }, expected = "\"col0\", \"col1\"")
    }

    @Test
    fun `test sqlQuestions`() {
        assertEquals(actual = this.rb.sqlQuestions(), expected = "?, ?")
    }

    @Test
    fun `test queryvalues`() {
        assertEquals(
            actual = this.rb.queryValues(this.parent).toList(), expected = listOf(
                QueryValue(name = "col0", value = 0, jdbcType = JDBCType.INTEGER, encoder = NumberTS.int.encoder),
                QueryValue(name = "col1", value = "1", jdbcType = JDBCType.VARCHAR, encoder = StringTS.string(100).encoder)
            )
        )
    }

}
