package com.urosjarc.dbmessiah.domain

import org.junit.jupiter.api.BeforeEach
import kotlin.test.Test
import kotlin.test.assertEquals

class Test_Table {

    data class Child(var pk: Int, val fk: String, val col: Float)
    data class Parent(var pk: String, val col: String)

    private lateinit var table: Table<Child>

    @BeforeEach
    fun init() {
        this.table = Table(primaryKey = Child::pk)
    }

    @Test
    fun `test name`() {
        //Primary key constraint should be injected automaticly
        assertEquals(expected = "Child", table.name)
    }

    @Test
    fun `test kclass`() {
        //Primary key constraint should be injected automaticly
        assertEquals(expected = Child::class, table.kclass)
    }

    @Test
    fun `test toString()`() {
        //Primary key constraint should be injected automaticly
        assertEquals(expected = "'${table.name}'", table.toString())
    }

}
