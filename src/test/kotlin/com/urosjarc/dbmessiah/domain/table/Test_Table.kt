package com.urosjarc.dbmessiah.domain.table
import com.urosjarc.dbmessiah.domain.columns.C
import org.junit.jupiter.api.BeforeEach
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.test.Test
import kotlin.test.assertEquals

class Test_Table {

    data class Child(var pk: Int, val fk: String, val col: Float)
    data class Parent(var pk: String, val col: String)

    private lateinit var table: Table<Any>

    @BeforeEach
    @Suppress("UNCHECKED_CAST")
    fun init() {
        this.table = Table(
            primaryKey = Child::pk,
            foreignKeys = listOf(Child::fk to Parent::pk) as List<Pair<KProperty1<Any, *>, KClass<*>>>,
            constraints = listOf(Child::fk to listOf(C.UNIQUE)) as List<Pair<KProperty1<Any, *>, List<C>>>,
        )
    }

    @Test
    fun `test init {}`() {
        //Primary key constraint should be injected automaticly
        val constraint: Pair<KProperty1<Any, *>, List<C>> = this.table.constraints.last()
        assertEquals(expected = Child::pk as KProperty1<Any, *>, actual = constraint.first)
        assertEquals(expected = listOf(C.AUTO_INC), actual = constraint.second)
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
    fun `test primaryKeyConstraints`() {
        //Primary key constraint should be injected automaticly
        assertEquals(expected = listOf(C.AUTO_INC), table.primaryKeyConstraints)
    }
    @Test
    fun `test toString()`() {
        //Primary key constraint should be injected automaticly
        assertEquals(expected = "'${table.name}'", table.toString())
    }

    @Test
    fun `test constraintsFor()`() {
        //Primary key constraint should be injected automaticly
        assertEquals(expected = listOf(C.UNIQUE), table.constraintsFor(Child::fk))
        assertEquals(expected = listOf(C.AUTO_INC), table.constraintsFor(Child::pk))
    }
}
