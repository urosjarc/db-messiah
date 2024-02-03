package com.urosjarc.dbmessiah.domain.call

import com.urosjarc.dbmessiah.domain.queries.QueryValue
import com.urosjarc.dbmessiah.exceptions.DbValueException
import com.urosjarc.dbmessiah.types.NumberTS
import com.urosjarc.dbmessiah.types.StringTS
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.assertThrows
import java.sql.JDBCType
import kotlin.reflect.KProperty1
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class Test_ProcedureArg {

    private lateinit var pArg0Copy: ProcedureArg
    private lateinit var pArg0: ProcedureArg
    private lateinit var pArg1: ProcedureArg
    private lateinit var procedure: Procedure

    data class TestProcedure(var arg0: Int, val arg1: String)
    data class TestProcedure2(var arg0: Int, val arg1: String)

    @BeforeEach
    @Suppress("UNCHECKED_CAST")
    fun init() {
        pArg0 = ProcedureArg(
            kprop = TestProcedure::arg0 as KProperty1<Any, Any?>,
            dbType = "INT",
            jdbcType = JDBCType.INTEGER,
            encoder = NumberTS.Int.encoder,
            decoder = NumberTS.Int.decoder
        )
        pArg0Copy = ProcedureArg(
            kprop = TestProcedure::arg0 as KProperty1<Any, Any?>,
            dbType = "INT",
            jdbcType = JDBCType.INTEGER,
            encoder = NumberTS.Int.encoder,
            decoder = NumberTS.Int.decoder
        )
        pArg1 = ProcedureArg(
            kprop = TestProcedure::arg1 as KProperty1<Any, Any?>,
            dbType = "INT",
            jdbcType = JDBCType.VARCHAR,
            encoder = NumberTS.Int.encoder,
            decoder = NumberTS.Int.decoder
        )
        procedure = Procedure(
            schema = "main",
            kclass = Test_Procedure.TestProcedure::class, args = listOf(pArg0)
        )

    }

    @Test
    fun `test inited`() {
        assertEquals(expected = true, actual = pArg0.inited)
        assertEquals(expected = false, actual = pArg1.inited)
    }

    @Test
    fun `test path`() {
        pArg0.procedure = procedure
        assertEquals(expected = "'TestProcedure'.'arg0'", actual = pArg0.path)
    }

    @Test
    fun `test name`() {
        pArg0.procedure = procedure
        assertEquals(expected = "'arg0'", actual = pArg0.name)
    }

    @Test
    fun `test kclass`() {
        assertEquals(expected = Int::class, actual = pArg0.kclass)
    }


    @Test
    fun `test hash`() {
        assertEquals(expected = pArg0.hash, actual = pArg0.hashCode())
    }

    @Test
    fun `test equals`() {
        assertEquals(expected = pArg0, actual = pArg0Copy)
        assertNotEquals(illegal = pArg0, actual = pArg1)
    }

    @Test
    fun `test toString()`() {
        pArg0.procedure = procedure
        assertEquals(expected = "Arg(name='arg0', dbType='INT', jdbcType='INTEGER')", actual = pArg0.toString())
    }

    @Test
    fun `test queryValue()`() {
        pArg0.procedure = procedure
        pArg1.procedure = procedure
        val testProcedure = TestProcedure(arg0 = 0, arg1 = "arg1_value")
        assertEquals(
            actual = this.pArg0.queryValue(testProcedure),
            expected = QueryValue(name = "'arg0'", value = 0, jdbcType = JDBCType.INTEGER, encoder = NumberTS.Int.encoder)
        )
        assertEquals(
            actual = this.pArg1.queryValue(testProcedure),
            expected = QueryValue(name = "'arg1'", value = "arg1_value", jdbcType = JDBCType.VARCHAR, encoder = StringTS.String(0).encoder)
        )

        val testProcedure2 = TestProcedure2(arg0 = 0, arg1 = "arg1_value")
        val e = assertThrows<DbValueException> {
            this.pArg1.queryValue(testProcedure2)
        }
        assertContains(
            charSequence = e.message.toString(),
            " Trying to get value 'val com.urosjarc.dbmessiah.domain.call.Test_ProcedureArg.TestProcedure.arg1: kotlin.String' but failed! " +
                    "Probably because receiving object is missing matching property or " +
                    "property does not belong to the receiver: TestProcedure2(arg0=0, arg1=arg1_value)",
            message = e.toString()
        )
    }

    @Test
    fun `test setValue()`() {
        pArg0.procedure = procedure
        pArg1.procedure = procedure

        /**
         * Normal usage
         */
        val testProcedure = TestProcedure(arg0 = 0, arg1 = "arg1_value")
        this.pArg0.setValue(testProcedure, 1)
        assertEquals(expected = 1, testProcedure.arg0)

        /**
         * Bad usage
         */
        val testProcedure2 = TestProcedure2(arg0 = 0, arg1 = "arg1_value")
        val e0 = assertThrows<DbValueException> {
            this.pArg1.setValue(testProcedure, "arg1_2value")
        }
        assertEquals(expected = "arg1_value", testProcedure.arg1)
        assertContains(
            charSequence = e0.message.toString(),
            "Trying to set property 'val com.urosjarc.dbmessiah.domain.call.Test_ProcedureArg.TestProcedure.arg1: kotlin.String' " +
                    "to 'arg1_2value' but the property is probably immutable",
            message = e0.toString()
        )


        /**
         * Bad usage
         */
        val e1 = assertThrows<DbValueException> {
            this.pArg0.setValue(testProcedure2, "arg1_2value")
        }
        assertEquals(expected = "arg1_value", testProcedure.arg1)
        assertContains(
            charSequence = e1.message.toString(),
            "Trying to set property 'var com.urosjarc.dbmessiah.domain.call.Test_ProcedureArg.TestProcedure.arg0: kotlin.Int' " +
                    "to 'arg1_2value' but failed! Probably because incompatible types " +
                    "or receiving object is missing matching property " +
                    "or property does not belong to the receiver: TestProcedure2(arg0=0, arg1=arg1_value)",
            message = e1.toString()
        )
    }

    @Test
    fun `test getValue()`() {
        pArg0.procedure = procedure
        pArg1.procedure = procedure

        /**
         * Normal usage
         */
        val testProcedure = TestProcedure(arg0 = 321, arg1 = "arg1_value")
        assertEquals(expected = 321, actual = this.pArg0.getValue(testProcedure))

        /**
         * Bad usage
         */
        val testProcedure2 = TestProcedure2(arg0 = 0, arg1 = "arg1_value")
        val e1 = assertThrows<DbValueException> {
            this.pArg1.getValue(testProcedure2)
        }

        assertContains(
            charSequence = e1.message.toString(),
            "Trying to get value 'val com.urosjarc.dbmessiah.domain.call.Test_ProcedureArg.TestProcedure.arg1: kotlin.String' but failed! Probably because receiving object is missing matching property or property does not belong to the receiver: TestProcedure2(arg0=0, arg1=arg1_value)",
            message = e1.toString()
        )
    }
}
