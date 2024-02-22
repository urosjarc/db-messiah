package com.urosjarc.dbmessiah.domain.call

import com.urosjarc.dbmessiah.domain.querie.QueryValue
import com.urosjarc.dbmessiah.types.NumberTS
import com.urosjarc.dbmessiah.types.StringTS
import org.junit.jupiter.api.BeforeEach
import java.sql.JDBCType
import kotlin.reflect.KProperty1
import kotlin.test.Test
import kotlin.test.assertEquals

class Test_Procedure {

    private lateinit var procedureArg0: ProcedureArg
    private lateinit var procedureArg1: ProcedureArg
    private lateinit var procedure: Procedure

    data class TestProcedure(var arg0: Int, val arg1: String)

    @BeforeEach
    fun init() {
        procedureArg0 = ProcedureArg(
            kprop = TestProcedure::arg0 as KProperty1<Any, Any?>,
            dbType = "INT",
            jdbcType = JDBCType.INTEGER,
            encoder = NumberTS.int.encoder,
            decoder = NumberTS.int.decoder
        )
        procedureArg1 = ProcedureArg(
            kprop = TestProcedure::arg1 as KProperty1<Any, Any?>,
            dbType = "INT",
            jdbcType = JDBCType.VARCHAR,
            encoder = NumberTS.int.encoder,
            decoder = NumberTS.int.decoder
        )
        procedure = Procedure(
            schema = "main",
            kclass = TestProcedure::class, args = listOf(
                procedureArg0,
                procedureArg1
            )
        )
        procedureArg0.procedure = procedure
        procedureArg1.procedure = procedure


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
            expected = QueryValue(name = "arg0", value = 3, jdbcType = JDBCType.INTEGER, encoder = NumberTS.int.encoder)
        )
        assertEquals(
            actual = queryValues[1],
            expected = QueryValue(name = "arg1", value = "pValue", jdbcType = JDBCType.VARCHAR, encoder = StringTS.string(0).encoder)
        )
    }
}
