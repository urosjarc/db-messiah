package com.urosjarc.dbmessiah.builders

import com.urosjarc.dbmessiah.data.Query
import com.urosjarc.dbmessiah.data.QueryValue
import com.urosjarc.dbmessiah.domain.Table
import com.urosjarc.dbmessiah.impl.sqlite.SqliteSerializer
import com.urosjarc.dbmessiah.serializers.AllTS
import com.urosjarc.dbmessiah.serializers.NumberTS
import com.urosjarc.dbmessiah.serializers.StringTS
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.sql.JDBCType
import kotlin.test.assertEquals

class Test_QueryBuilder {
    private lateinit var parent: Parent
    private lateinit var ser: SqliteSerializer
    private lateinit var qb: QueryBuilder<Parent>

    data class Parent(var col0: Int?, val col1: String)

    @BeforeEach
    fun init() {
        this.ser = SqliteSerializer(
            tables = listOf(
                Table(Parent::col0)
            ),
            globalSerializers = AllTS.basic
        )
        this.parent = Parent(col0 = 0, col1 = "1")
        this.qb = QueryBuilder(input = this.parent, ser = ser)
    }

    @Test
    fun `test input`() {
        assertEquals(expected = "?", actual = this.qb.input(Parent::col0))
    }

    @Test
    fun `test build`() {
        assertEquals(actual = this.qb.build("SQL"), expected = Query("SQL"))

        //First input
        this.qb.input(Parent::col0)
        assertEquals(
            actual = this.qb.build("SQL"),
            expected = Query("SQL", QueryValue(name = "col0", value = 0, jdbcType = JDBCType.INTEGER, encoder = NumberTS.int.encoder))
        )

        //Second input
        this.qb.input(Parent::col1)
        assertEquals(
            actual = this.qb.build("SQL"),
            expected = Query(
                "SQL",
                QueryValue(name = "col0", value = 0, jdbcType = JDBCType.INTEGER, encoder = NumberTS.int.encoder),
                QueryValue(name = "col1", value = "1", jdbcType = JDBCType.VARCHAR, encoder = StringTS.string(100).encoder)
            )
        )
    }

}
