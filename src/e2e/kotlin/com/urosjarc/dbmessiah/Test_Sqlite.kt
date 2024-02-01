package com.urosjarc.dbmessiah

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
import kotlin.test.*


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
            // Select first 5
            val select0 = it.select(kclass = Child::class, page = Page(number = 0, orderBy = Child::pk, limit = 5))
            assertEquals(expected = this.children.subList(0, 5), actual = select0)

            // Select first 7
            val select1 = it.select(kclass = Child::class, page = Page(number = 0, orderBy = Child::pk, limit = 7))
            assertEquals(expected = this.children.subList(0, 7), actual = select1)

            // Select 3 page of 7
            val select2 = it.select(kclass = Child::class, page = Page(number = 2, orderBy = Child::pk, limit = 7))
            assertEquals(expected = this.children.subList(14, 14 + 7), actual = select2)

            // Select 3 page of 4
            val select3 = it.select(kclass = Child::class, page = Page(number = 2, orderBy = Child::pk, limit = 4))
            assertEquals(expected = this.children.subList(8, 8 + 4), actual = select3)

            // It should be consistent
            val select4 = it.select(kclass = Child::class, page = Page(number = 2, orderBy = Child::pk, limit = 4))
            assertEquals(expected = select3, actual = select4)
        }
    }

    @Test
    fun `test insert`() {
        service.query {
            //Get current all parents
            val parents = it.select(kclass = Parent::class)
            assertEquals(expected = this.parents, actual = parents)

            //Create new object
            // does not allready have primary key
            //New object is not contained inside parents
            val newObj = Parent(col = "NEW")
            assertEquals(expected = null, actual = newObj.pk)
            assertFalse(actual = parents.contains(newObj))

            //Insert new object
            assertTrue(actual = it.insert(obj = newObj))

            //Check if primary key was updated
            assertTrue(actual = newObj.pk!! > 0)
            val postParents = it.select(kclass = Parent::class)
            assertTrue(actual = postParents.contains(newObj))

            //Check consistency of inserted command
            val preParents = postParents.toMutableList()
            preParents.remove(newObj)
            assertEquals(actual = preParents, expected = parents)

            //Try to insert element again
            assertFalse(actual = it.insert(obj = newObj))

            //Parents really stayed as they were before
            val postParents2 = it.select(kclass = Parent::class)
            assertEquals(actual = postParents2, expected = postParents)
        }
    }

    @Test
    fun `test insertBatch`() {
        service.query {
            //Get current all parents
            val parents = it.select(kclass = Parent::class)
            assertEquals(expected = this.parents, actual = parents)

            //Create new object
            // does not allready have primary key
            //New object is not contained inside parents
            val newObj0 = Parent(col = "NEW0")
            val newObj1 = Parent(col = "NEW1")
            assertEquals(expected = null, actual = newObj0.pk)
            assertEquals(expected = null, actual = newObj1.pk)
            assertFalse(actual = parents.contains(newObj0))
            assertFalse(actual = parents.contains(newObj1))

            //Insert new object
            assertEquals(actual = it.insertBatch(newObj0, newObj1), expected = 2)

            //Check if primary key are not updated after batch insert
            assertEquals(expected = null, actual = newObj0.pk)
            assertEquals(expected = null, actual = newObj1.pk)

            val postParents = it.select(kclass = Parent::class)
            assertEquals(expected = parents.size + 2, actual = postParents.size)
            val last2Parents = postParents.takeLast(2).map {
                it.pk = null
                it
            }.reversed()
            assertEquals(expected = listOf(newObj1, newObj0), actual = last2Parents)

            //You can insert elements if primary key is not set
            newObj0.pk = 0
            newObj1.pk = 0

            //Get snapshot of parents before trying to insert
            val postParents2 = it.select(kclass = Parent::class)
            assertEquals(actual = it.insertBatch(newObj0, newObj1), expected = 0)

            //Parents really stayed as they were before
            val postParents3 = it.select(kclass = Parent::class)

            //Check if post and pre matches
            assertEquals(actual = postParents2, expected = postParents3)
        }
    }

    @Test
    fun `test update`() {
        service.query {
            //Get current all parents
            val parents = it.select(kclass = Parent::class)
            assertEquals(expected = this.parents, actual = parents)

            //Update first
            parents[0].col = "UPDATE"
            assertTrue(it.update(obj = parents[0]))

            //Get current all parents
            val postParents = it.select(kclass = Parent::class)
            assertEquals(expected = parents, actual = postParents)

            //Object should not be updated if has no primary key
            parents[1].pk = null
            parents[1].col = "UPDATE2"
            assertFalse(it.update(obj = parents[1]))

            //Update should not change anything in db
            val postParents2 = it.select(kclass = Parent::class)
            assertEquals(expected = postParents, actual = postParents2)
        }
    }

    @Test
    fun `test updateBatch`() {
        service.query {
            //Get current all parents
            val parents = it.select(kclass = Parent::class)
            assertEquals(expected = this.parents, actual = parents)

            parents[0].col = "UPDATE0"
            parents[1].col = "UPDATE1"

            //Update
            assertEquals(expected = 2, actual = it.updateBatch(parents[0], parents[1]))

            //List should be equal
            val postParents0 = it.select(kclass = Parent::class)
            assertEquals(expected = parents, actual = postParents0)

            //Insert without primary key
            postParents0[2].pk = null
            postParents0[3].pk = null

            //Create snapshot before inserting for comparison
            val postParents1 = it.select(kclass = Parent::class)
            assertEquals(expected = 0, actual = it.updateBatch(postParents0[2], postParents0[3]))

            //List should be equal
            val postParents2 = it.select(kclass = Parent::class)
            assertEquals(expected = postParents1, actual = postParents2)
        }
    }

    @Test
    fun `test delete`() {
        service.query {
            //Get current all parents
            val parents = it.select(kclass = Parent::class)
            assertEquals(expected = this.parents, actual = parents)

            //Check if primary exists and is greater than 0
            assertTrue(parents[0].pk!! > 0)

            //Delete first
            assertTrue(it.delete(obj = parents[0]))

            //Check if primary was set to null
            assertEquals(actual = parents[0].pk, expected = null)

            //Get current all parents
            val postParents = it.select(kclass = Parent::class).toMutableList()
            assertEquals(expected = parents.size, actual = postParents.size + 1)

            //Check if other are as they were before
            val filteredParents = parents.filter { it.pk != null }
            assertEquals(expected = filteredParents, actual = postParents)

            //Object should not be deleted if has no primary key
            parents[1].pk = null
            assertFalse(it.delete(obj = parents[1]))

            //Update should not change anything in db
            val postParents2 = it.select(kclass = Parent::class)
            assertEquals(expected = postParents, actual = postParents2)
        }
    }

    @Test
    fun `test delete kclass`() {
        service.query {
            //Get current all parents
            val parents = it.select(kclass = Parent::class)
            assertEquals(expected = this.parents, actual = parents)

            //Check if number of deletes matches original size
            assertTrue(parents.isNotEmpty())
            assertEquals(expected = parents.size, actual = it.delete(kclass = Parent::class))

            //Check if no parent is left
            val postParents = it.select(kclass = Parent::class)
            assertTrue(actual = postParents.isEmpty())

            //If again delete return 0
            assertEquals(expected = 0, actual = it.delete(kclass = Parent::class))
        }
    }

    @Test
    fun `test deleteBatch`() {
        service.query {
            //Get current all parents
            val children = it.select(kclass = Child::class)
            assertEquals(expected = this.children, actual = children)

            //Delete
            assertEquals(expected = 2, actual = it.deleteBatch(children[0], children[1]))

            //Primary keys are not deleted
            assertTrue(actual = children[0].pk!! > 0)
            assertTrue(actual = children[1].pk!! > 0)

            //List should not be equal
            val postChildren0 = it.select(kclass = Child::class)
            assertEquals(expected = children.size, actual = postChildren0.size + 2)
            val filteredChildren = children.drop(2)
            assertEquals(expected = filteredChildren, actual = postChildren0)

            //Insert without primary key
            postChildren0[2].pk = null
            postChildren0[3].pk = null

            //Create snapshot before inserting for comparison
            val postChildren1 = it.select(kclass = Child::class)
            assertEquals(expected = 0, actual = it.updateBatch(postChildren0[2], postChildren0[3]))

            //List should be equal
            val postChildren2 = it.select(kclass = Child::class)
            assertEquals(expected = postChildren1, actual = postChildren2)
        }
    }

    @Test
    fun `test execute`() {
        return
        service.query {

            it.execute {
                """
                    DO TEST THIS STUFF EXTENSIVLI FOR EVERY POSIBLE QUERY EVEN FOR MULTIPLE QUERIES!!!!!!!
                """.trimIndent()
            }

        }
    }
}
