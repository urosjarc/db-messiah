package com.urosjarc.dbmessiah.data

import org.junit.jupiter.api.Assertions.assertFalse
import java.sql.JDBCType
import java.sql.ResultSet
import kotlin.reflect.KProperty1
import kotlin.test.Test
import kotlin.test.assertTrue

class Test_PrimaryColumn {
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

        val k: UInt,
        val l: UInt = 2u,
        val m: UInt?,
        val n: UInt? = 2u,
        val o: UInt? = null,

        var p: UInt,
        var r: UInt = 2u,
        var s: UInt?,
        var t: UInt? = 2u,
        var u: UInt? = null,
    )

    private fun AUTO_INC(kprop: KProperty1<*, *>): Boolean = PrimaryColumn(
        kprop = kprop as KProperty1<Any, Any?>,
        dbType = "",
        jdbcType = JDBCType.VARCHAR,
        encoder = { _, _, _ -> },
        decoder = { rs: ResultSet, i: Int, _: DecodeInfo -> rs.getString(i) }
    ).autoInc

    @Test
    fun `test autoIncrement`() {
        assertTrue(this.AUTO_INC(kprop = Data::h))
        assertTrue(this.AUTO_INC(kprop = Data::i))
        assertTrue(this.AUTO_INC(kprop = Data::j))
        assertTrue(this.AUTO_INC(kprop = Data::s))
        assertTrue(this.AUTO_INC(kprop = Data::t))
        assertTrue(this.AUTO_INC(kprop = Data::u))

        assertFalse(this.AUTO_INC(kprop = Data::a))
        assertFalse(this.AUTO_INC(kprop = Data::b))
        assertFalse(this.AUTO_INC(kprop = Data::c))
        assertFalse(this.AUTO_INC(kprop = Data::d))
        assertFalse(this.AUTO_INC(kprop = Data::e))
        assertFalse(this.AUTO_INC(kprop = Data::f))
        assertFalse(this.AUTO_INC(kprop = Data::g))
        assertFalse(this.AUTO_INC(kprop = Data::k))
        assertFalse(this.AUTO_INC(kprop = Data::l))
        assertFalse(this.AUTO_INC(kprop = Data::m))
        assertFalse(this.AUTO_INC(kprop = Data::n))
        assertFalse(this.AUTO_INC(kprop = Data::o))
        assertFalse(this.AUTO_INC(kprop = Data::p))
        assertFalse(this.AUTO_INC(kprop = Data::r))
    }
}
