package com.urosjarc.dbmessiah.extend

import kotlin.test.Test
import kotlin.test.assertEquals

class Test_List {

    @Test
    fun `test getValue()`() {
        assertEquals(actual = listOf(1, 2, 3, 4, 5, 2, 3, 3, 3, 3).ext_notUnique, expected = mapOf(2 to 2, 3 to 5) as Map<Any?, Int>)
        assertEquals(actual = listOf(1, 2, 3, 4).ext_notUnique, expected = mapOf())
    }

}
