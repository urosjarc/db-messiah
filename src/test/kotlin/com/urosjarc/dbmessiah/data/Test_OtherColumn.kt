package com.urosjarc.dbmessiah.data

import org.junit.jupiter.api.Assertions.assertFalse
import java.sql.JDBCType
import java.sql.ResultSet
import kotlin.reflect.KProperty1
import kotlin.test.Test
import kotlin.test.assertTrue

class Test_OtherColumn {

    data class Data(
        val a: Int,
        val b: Int = 2,
        val c: Int?,
        val d: Int? = 2,
        val e: Int? = null,

        var f: Int,
        var g: Int = 2,
        var h: Int?,
        var i: Int? = 2,
        var j: Int? = null,
    )

    private fun NOT_NULL(kprop: KProperty1<*, *>): Boolean = OtherColumn(
        unique = false,
        kprop = kprop as KProperty1<Any, Any?>,
        dbType = "",
        jdbcType = JDBCType.VARCHAR,
        encoder = { _, _, _ -> },
        decoder = { rs: ResultSet, i: Int, _: DecodeInfo -> rs.getString(i) }
    ).notNull

    @Test
    fun `test notNull`() {
        assertTrue(this.NOT_NULL(kprop = Data::a))
        assertTrue(this.NOT_NULL(kprop = Data::b))
        assertTrue(this.NOT_NULL(kprop = Data::f))
        assertTrue(this.NOT_NULL(kprop = Data::g))

        assertFalse(this.NOT_NULL(kprop = Data::c))
        assertFalse(this.NOT_NULL(kprop = Data::d))
        assertFalse(this.NOT_NULL(kprop = Data::e))
        assertFalse(this.NOT_NULL(kprop = Data::h))
        assertFalse(this.NOT_NULL(kprop = Data::i))
        assertFalse(this.NOT_NULL(kprop = Data::j))
    }
}
