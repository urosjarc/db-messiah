package com.urosjarc.dbmessiah.domain.queries

import com.urosjarc.dbmessiah.domain.querie.Query
import com.urosjarc.dbmessiah.domain.querie.QueryValue
import com.urosjarc.dbmessiah.types.StringTS
import org.junit.jupiter.api.BeforeEach
import java.sql.JDBCType
import kotlin.test.Test
import kotlin.test.assertEquals

class Test_Query {

    private lateinit var query: Query

    @BeforeEach
    fun init() {
        this.query = Query(
            sql = "SELECT * FROM Table", values = arrayOf(
                QueryValue(name = "name1", value = "value1" as Any?, jdbcType = JDBCType.VARCHAR, encoder = StringTS.String(0).encoder),
                QueryValue(name = "name2", value = 123 as Any?, jdbcType = JDBCType.INTEGER, encoder = StringTS.String(0).encoder)
            )
        )

    }

    @Test
    fun `test toString()`() {
        assertEquals(
            expected = "\n\nSELECT * FROM Table\n\n\t1) name1: VARCHAR = 'value1'\n\t2) name2: INTEGER = 123\n",
            actual = query.toString()
        )
    }

}
