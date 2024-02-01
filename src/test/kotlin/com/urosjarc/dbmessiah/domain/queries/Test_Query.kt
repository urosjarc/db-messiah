package com.urosjarc.dbmessiah.domain.queries

import com.urosjarc.dbmessiah.types.StringTS
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestInstance
import java.sql.JDBCType
import kotlin.test.Test
import kotlin.test.assertEquals

class Test_Query {

    private lateinit var query: Query

    @BeforeEach
    fun init(){
        this.query = Query(
            sql = "SELECT * FROM Table", values = arrayOf(
                QueryValue(name = "name1", value = "value1" as Any?, jdbcType = JDBCType.VARCHAR, encoder = StringTS.String(0).encoder),
                QueryValue(name = "name2", value = 123 as Any?, jdbcType = JDBCType.INTEGER, encoder = StringTS.String(0).encoder)
            )
        )

    }

    @Test
    fun `test getValue()`() {
        assertEquals(expected = """
SELECT * FROM Table

	1) name1: VARCHAR = 'value1'
	2) name2: INTEGER = 123

        """.trimIndent(), actual = query.toString())
    }

}
