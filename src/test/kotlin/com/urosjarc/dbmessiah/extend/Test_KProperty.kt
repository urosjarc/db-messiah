package com.urosjarc.dbmessiah.extend

import kotlin.test.Test
import kotlin.test.assertEquals

class Test_KProperty {

    data class Data(
        val a: Int,
        val b: String = "b",
        val c: Char?,
        val d: Float? = 3.4f,
        val e: Double? = null,

        var f: Int,
        var g: String = "b",
        var h: Char?,
        var i: Float? = 3.4f,
        var j: Double? = null,
        var k: UInt,
    )
    @Test
    fun `test ext_owner()`() {
        assertEquals(actual = Data::a.ext_owner, expected = Data::class)
        assertEquals(actual = Data::b.ext_owner, expected = Data::class)
        assertEquals(actual = Data::c.ext_owner, expected = Data::class)
    }

    @Test
    fun `test ext_isMutable()`() {
        assertEquals(actual = Data::a.ext_isMutable, expected = false)
        assertEquals(actual = Data::b.ext_isMutable, expected = false)
        assertEquals(actual = Data::c.ext_isMutable, expected = false)
        assertEquals(actual = Data::d.ext_isMutable, expected = false)
        assertEquals(actual = Data::e.ext_isMutable, expected = false)
        assertEquals(actual = Data::f.ext_isMutable, expected = true)
        assertEquals(actual = Data::g.ext_isMutable, expected = true)
        assertEquals(actual = Data::h.ext_isMutable, expected = true)
        assertEquals(actual = Data::i.ext_isMutable, expected = true)
        assertEquals(actual = Data::j.ext_isMutable, expected = true)
        assertEquals(actual = Data::k.ext_isMutable, expected = true)
    }

    @Test
    fun `test ext_isOptional()`() {
        assertEquals(actual = Data::a.ext_isOptional, expected = false)
        assertEquals(actual = Data::b.ext_isOptional, expected = false)
        assertEquals(actual = Data::c.ext_isOptional, expected = true)
        assertEquals(actual = Data::d.ext_isOptional, expected = true)
        assertEquals(actual = Data::e.ext_isOptional, expected = true)
        assertEquals(actual = Data::f.ext_isOptional, expected = false)
        assertEquals(actual = Data::g.ext_isOptional, expected = false)
        assertEquals(actual = Data::h.ext_isOptional, expected = true)
        assertEquals(actual = Data::i.ext_isOptional, expected = true)
        assertEquals(actual = Data::j.ext_isOptional, expected = true)
        assertEquals(actual = Data::k.ext_isOptional, expected = false)
    }
    @Test
    fun `test ext_isWholeNumber()`() {
        assertEquals(actual = Data::a.ext_isWholeNumber, expected = true)
        assertEquals(actual = Data::b.ext_isWholeNumber, expected = false)
        assertEquals(actual = Data::c.ext_isWholeNumber, expected = false)
        assertEquals(actual = Data::d.ext_isWholeNumber, expected = false)
        assertEquals(actual = Data::e.ext_isWholeNumber, expected = false)
        assertEquals(actual = Data::f.ext_isWholeNumber, expected = true)
        assertEquals(actual = Data::g.ext_isWholeNumber, expected = false)
        assertEquals(actual = Data::h.ext_isWholeNumber, expected = false)
        assertEquals(actual = Data::i.ext_isWholeNumber, expected = false)
        assertEquals(actual = Data::j.ext_isWholeNumber, expected = false)
        assertEquals(actual = Data::k.ext_isWholeNumber, expected = true)
    }



}
