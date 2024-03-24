package com.urosjarc.dbmessiah.domain

import org.junit.jupiter.api.BeforeEach
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.test.Test
import kotlin.test.assertEquals

class Test_Table {

    data class Child(var pk: Int, val fk: String, val col: Float)
    data class Parent(var pk: String, val col: String)

    private lateinit var table: Table<Child>

    @JvmInline
    value class Id<T>(val value: UUID = UUID.randomUUID())
    data class Other(
        val id: Id<Other> = Id(),
        val parent_fk: Id<Parent>,
        val child_fk: Id<Child>,
        val col0: String,
        val col1: Int,
    )

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

    @Test
    fun `test extractForeignKeys`() {

        /** Second */
        val fk2 = Table.getInlineTypedForeignKeys(primaryKey = Other::id)
        assertEquals(
            actual = fk2, expected = mutableListOf(
                Other::child_fk to Child::class,
                Other::parent_fk to Parent::class,
            ) as MutableList<Pair<KProperty1<Other, *>, KClass<*>>>
        )

    }
}
