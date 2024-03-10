package com.urosjarc.dbmessiah.extend

import kotlin.test.Test
import kotlin.test.assertEquals

class Test_KClass {

    data class Testing(
        val a: Int,
        val b: String,
        val c: Float
    )
    class Testing2(
        a: Int,
        b: String,
        c: Float
    ) {
        val d = a
        val e = b
    }


    @Test
    fun `test ext_kparams()`() {
        assertEquals(actual = Testing::class.ext_kparams?.map { it.name }, expected = listOf("a", "b", "c"))
        assertEquals(actual = Testing2::class.ext_kparams?.map { it.name }, expected = listOf("a", "b", "c"))

    }
    @Test
    fun `test ext_kprops()`() {
        assertEquals(actual = Testing::class.ext_kprops.map { it.name }, expected = listOf("a", "b", "c"))
        assertEquals(actual = Testing2::class.ext_kprops.map { it.name }, expected = listOf("d", "e"))
    }
}
