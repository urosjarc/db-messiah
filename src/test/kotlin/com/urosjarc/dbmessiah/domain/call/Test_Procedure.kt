package com.urosjarc.dbmessiah.domain.call

import com.urosjarc.dbmessiah.domain.queries.QueryValue
import com.urosjarc.dbmessiah.domain.table.Escaper
import com.urosjarc.dbmessiah.types.NumberTS
import com.urosjarc.dbmessiah.types.StringTS
import org.junit.jupiter.api.BeforeEach
import java.sql.JDBCType
import kotlin.reflect.KProperty1
import kotlin.test.Test
import kotlin.test.assertEquals

class Test_Procedure {

    private lateinit var procedure: Procedure

    data class TestProcedure(var arg0: Int, val arg1: String)
    data class Test2Procedure(var id: Int, val property: String)

    @BeforeEach
    fun init() {
        procedure = Procedure(
            escaper = Escaper(),
            kclass = TestProcedure::class, args = listOf(
                ProcedureArg(
                    kprop = TestProcedure::arg0 as KProperty1<Any, Any?>,
                    dbType = "INT",
                    jdbcType = JDBCType.INTEGER,
                    encoder = NumberTS.Int.encoder,
                    decoder = NumberTS.Int.decoder
                ),
                ProcedureArg(
                    kprop = TestProcedure::arg1 as KProperty1<Any, Any?>,
                    dbType = "INT",
                    jdbcType = JDBCType.VARCHAR,
                    encoder = NumberTS.Int.encoder,
                    decoder = NumberTS.Int.decoder
                )
            )
        )


    }

    @Test
    fun `test getString()`() {
        assertEquals(expected = "TestProcedure(arg0: Int, arg1: String)", actual = procedure.toString())
    }

    @Test
    fun `test sqlArguments()`() {
        assertEquals(expected = "?, ?", actual = procedure.sqlArguments())
    }

    @Test
    fun `test queryValues()`() {
        val entity = TestProcedure(arg0 = 3, arg1 = "pValue")
        val queryValues = this.procedure.queryValues(entity)
        assertEquals(expected = 2, queryValues.size)
        assertEquals(
            actual = queryValues[0],
            expected = QueryValue(name = "arg0", value = 3, jdbcType = JDBCType.INTEGER, encoder = NumberTS.Int.encoder)
        )
        assertEquals(
            actual = queryValues[1],
            expected = QueryValue(name = "arg1", value = "pValue", jdbcType = JDBCType.VARCHAR, encoder = StringTS.String(0).encoder)
        )
    }
}
