package com.urosjarc.dbmessiah

import com.urosjarc.dbmessiah.*
import com.urosjarc.dbmessiah.domain.queries.Page
import com.urosjarc.dbmessiah.exceptions.TesterException
import com.urosjarc.dbmessiah.impl.sqlite.SqliteSerializer
import com.urosjarc.dbmessiah.types.AllTS
import com.zaxxer.hikari.HikariConfig
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.reflect.KClass
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue


class Test_Sqlite {
    var children = mutableListOf<Child>()
    var parents = mutableListOf<Parent>()

    companion object {
        private lateinit var service: DbMessiahService
        private lateinit var serializer: SqliteSerializer
        private lateinit var sqliteConfig: HikariConfig

        @JvmStatic
        @BeforeAll
        fun init() {
            sqliteConfig = HikariConfig().apply {
                jdbcUrl = "jdbc:sqlite::memory:"
                username = null
                password = null
            }

            serializer = SqliteSerializer(
                schemas = listOf(testSchema),
                globalSerializers = AllTS.basic,
                globalOutputs = listOf(TestOutput::class),
                globalInputs = listOf(TestInput::class)
            ); service = DbMessiahService(
                config = sqliteConfig,
                serializer = serializer
            )
        }
    }

    @BeforeEach
    fun prepare() {
        //Reseting tables
        service.query {
            it.create(Child::class)
            it.create(Parent::class)
            it.delete(Child::class)
            it.delete(Parent::class)
        }

        val numParents = 5
        val numChildren = 5
        children = mutableListOf()
        parents = mutableListOf()

        //Inserting tables
        service.query {
            repeat(times = numParents) { p ->
                val parent = Parent.get(seed = p)
                parents.add(parent)
                val parentInserted = it.insert(obj = parent)
                if (parent.pk == null || !parentInserted) throw TesterException("Parent was not inserted: $parent")
                repeat(numChildren) { c ->
                    val child = Child.get(fk = parent.pk!!, seed = p * numChildren + c)
                    children.add(child)
                    val childInserted = it.insert(obj = child)
                    if (child.pk == null || !childInserted) throw TesterException("Children was not inserted: $child")
                }
            }

            //Testing current state
            val insertedParents = it.select(kclass = Parent::class)
            val insertedChildren = it.select(kclass = Child::class)

            if (insertedChildren != children || insertedParents != parents)
                throw TesterException("Test state does not match with expected state")
        }

    }

    private fun assertTableNotExists(q: QueryConnection, kclass: KClass<*>) {
        val e = assertThrows<Throwable> {
            q.select(kclass = kclass)
        }
        assertContains(charSequence = e.message.toString(), other = "missing database", message = e.toString())
    }

    @Test
    fun `test drop`() {
        service.query {
            //You can select
            it.select(kclass = Parent::class)

            //Drop
            it.drop(kclass = Parent::class)

            //You can't select on droped table
            this.assertTableNotExists(q = it, kclass = Parent::class)
        }
    }

    @Test
    fun `test create`() {
        service.query {
            //Get pre create state
            val preParents = it.select(kclass = Parent::class)
            assertTrue(actual = preParents.isNotEmpty())

            //Create table if allready created should not throw error
            assertEquals(actual = it.create(kclass = Parent::class), expected = 1)

            //Create table should not change previous state
            val postParents = it.select(kclass = Parent::class)
            assertEquals(actual = postParents, expected = preParents)

            //Drop
            assertEquals(actual = it.drop(Parent::class), expected = 1)

            //Select will create error
            this.assertTableNotExists(q = it, kclass = Parent::class)

            //Recreate table
            assertEquals(actual = it.create(kclass = Parent::class), expected = 1)

            //Now we can get elements
            assertTrue(it.select(kclass = Parent::class).isEmpty())
        }
    }

    @Test
    fun `test select`() {
        service.query {
            //It should be equal to inserted parents
            val selected0 = it.select(kclass = Parent::class)
            assertEquals(expected = this.parents, actual = selected0)

            //It should be consistent
            val selected1 = it.select(kclass = Parent::class)
            assertEquals(expected = selected0, actual = selected1)
        }
    }

    @Test
    fun `test select pk`() {
        service.query {
            //Should return expected
            val parent0 = it.select(kclass = Parent::class, pk = 1)
            assertEquals(expected = this.parents[0], actual = parent0)

            //It should be consistent
            val parent1 = it.select(kclass = Parent::class, pk = 1)
            assertEquals(expected = parent0, actual = parent1)

            //Should differ
            val parent2 = it.select(kclass = Parent::class, pk = 2)
            assertEquals(expected = this.parents[1], actual = parent2)
            assertNotEquals(illegal = parent1, actual = parent2)
        }
    }

    @Test
    fun `test select page`() {
        service.query {
            //Should return expected
            val select0 = it.select(kclass = Child::class, page = Page(number = 0, orderBy = Child::pk, limit = 5))
            assertEquals(expected = this.children.subList(0, 5), actual = select0)

            //Should return expected
            val select1 = it.select(kclass = Child::class, page = Page(number = 0, orderBy = Child::pk, limit = 7))
            assertEquals(expected = this.children.subList(0, 7), actual = select1)

            //Should return expected
            val select2 = it.select(kclass = Child::class, page = Page(number = 2, orderBy = Child::pk, limit = 7))
            assertEquals(expected = this.children.subList(14, 14 + 7), actual = select2)

            //Should return expected
            val select3 = it.select(kclass = Child::class, page = Page(number = 2, orderBy = Child::pk, limit = 4))
            assertEquals(expected = this.children.subList(8, 8 + 4), actual = select3)

            //Should be consistent
            val select4 = it.select(kclass = Child::class, page = Page(number = 2, orderBy = Child::pk, limit = 4))
            assertEquals(expected = select3, actual = select4)
        }
    }
}
