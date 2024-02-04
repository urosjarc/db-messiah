package com.urosjarc.dbmessiah

import com.urosjarc.dbmessiah.domain.queries.Page
import com.urosjarc.dbmessiah.domain.table.Table
import com.urosjarc.dbmessiah.exceptions.TesterException
import com.urosjarc.dbmessiah.impl.mariadb.MariaQueryConn
import com.urosjarc.dbmessiah.impl.mariadb.MariaSchema
import com.urosjarc.dbmessiah.impl.mariadb.MariaSerializer
import com.urosjarc.dbmessiah.impl.mariadb.MariaService
import com.urosjarc.dbmessiah.types.AllTS
import com.zaxxer.hikari.HikariConfig
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.reflect.KClass
import kotlin.test.*


open class Test_Maria {
    open var parents = mutableListOf<Parent>()
    open var children = mutableListOf<Child>()

    companion object {
        private lateinit var service: MariaService

        @JvmStatic
        @BeforeAll
        fun init() {
            service = MariaService(
                conf = HikariConfig().apply {
                    this.jdbcUrl = "jdbc:mariadb://localhost:3306"
                    this.username = "root"
                    this.password = "root"
                },
                ser = MariaSerializer(
                    schemas = listOf(
                        MariaSchema(
                            name = "main", tables = listOf(
                                Table(Parent::pk),
                                Table(
                                    Child::pk, foreignKeys = listOf(
                                        Child::fk to Parent::class
                                    )
                                )
                            )
                        )
                    ),
                    globalSerializers = AllTS.basic,
                    globalOutputs = listOf(Output::class),
                    globalInputs = listOf(Input::class)
                )
            )
        }
    }

    @BeforeEach
    fun prepare() {
        //Reseting tables
        service.query {
            it.query { "CREATE OR REPLACE SCHEMA main;" }
            it.query { "SET FOREIGN_KEY_CHECKS=0;" }

            it.drop(Child::class)
            it.drop(Parent::class)
            it.create(Parent::class)
            it.create(Child::class)
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
                val parentInserted = it.insert(row = parent)
                if (parent.pk == null || !parentInserted) throw TesterException("Parent was not inserted: $parent")
                repeat(numChildren) { c ->
                    val child = Child.get(fk = parent.pk!!, seed = p * numChildren + c)
                    children.add(child)
                    val childInserted = it.insert(row = child)
                    if (child.pk == null || !childInserted) throw TesterException("Children was not inserted: $child")
                }
            }

            //Testing current state
            val insertedParents = it.select(table = Parent::class)
            val insertedChildren = it.select(table = Child::class)

            if (insertedChildren != children || insertedParents != parents)
                throw TesterException("Test state does not match with expected state")
        }

        //Create procedures and disable foreign checks
        service.query {
            it.query {
                """
                        CREATE OR REPLACE PROCEDURE main.TestProcedure(parent_pk INT)
                        BEGIN
                            SELECT * FROM main.Parent WHERE pk = parent_pk;
                            SELECT * FROM main.Parent WHERE pk = parent_pk1;
                        END;
                    """.trimIndent()
            }
            it.query {
                """
                        CREATE OR REPLACE PROCEDURE main.TestProcedureEmpty()
                        BEGIN
                            SELECT * FROM main.Parent WHERE pk = 2;
                            SELECT * FROM main.Parent WHERE pk = 2;
                        END;
                    """.trimIndent()
            }
        }

    }

    private fun assertTableNotExists(q: MariaQueryConn, kclass: KClass<*>) {
        val e = assertThrows<Throwable> { q.select(table = kclass) }
        assertContains(
            charSequence = e.message.toString(),
            other = "Table 'main.Parent' doesn't exist",
            message = e.toString()
        )
    }

    @Test
    fun `test table drop`() = service.query {
        //You can select
        it.select(table = Parent::class)

        //Drop
        it.drop(table = Parent::class)

        //You can't select on droped table
        this.assertTableNotExists(q = it, kclass = Parent::class)
    }

    @Test
    fun `test table create`() = service.query {
        //Get pre create state
        val preParents = it.select(table = Parent::class)
        assertTrue(actual = preParents.isNotEmpty())

        //Create table if allready created should not throw error
        assertEquals(actual = it.create(table = Parent::class), expected = 0)

        //Create table should not change previous state
        val postParents = it.select(table = Parent::class)
        assertEquals(actual = postParents, expected = preParents)

        //Drop
        assertEquals(actual = it.drop(Parent::class), expected = 0)

        //Select will create error
        this.assertTableNotExists(q = it, kclass = Parent::class)

        //Recreate table
        assertEquals(actual = it.create(table = Parent::class), expected = 0)

        //Now we can get elements
        assertTrue(it.select(table = Parent::class).isEmpty())
    }

    @Test
    fun `test table delete`() = service.query {
        //Get current all parents
        val parents = it.select(table = Parent::class)
        assertEquals(expected = this.parents, actual = parents)

        //Check if number of deletes matches original size
        assertTrue(parents.isNotEmpty())
        assertEquals(expected = parents.size, actual = it.delete(table = Parent::class))

        //Check if no parent is left
        val postParents = it.select(table = Parent::class)
        assertTrue(actual = postParents.isEmpty())

        //If again delete return 0
        assertEquals(expected = 0, actual = it.delete(table = Parent::class))
    }

    @Test
    fun `test table select`() = service.query {
        //It should be equal to inserted parents
        val selected0 = it.select(table = Parent::class)
        assertEquals(expected = this.parents, actual = selected0)

        //It should be consistent
        val selected1 = it.select(table = Parent::class)
        assertEquals(expected = selected0, actual = selected1)
    }

    @Test
    fun `test table select page`() = service.query {
        // Select first 5
        val select0 = it.select(table = Child::class, page = Page(number = 0, orderBy = Child::pk, limit = 5))
        assertEquals(expected = this.children.subList(0, 5), actual = select0)

        // Select first 7
        val select1 = it.select(table = Child::class, page = Page(number = 0, orderBy = Child::pk, limit = 7))
        assertEquals(expected = this.children.subList(0, 7), actual = select1)

        // Select 3 page of 7
        val select2 = it.select(table = Child::class, page = Page(number = 2, orderBy = Child::pk, limit = 7))
        assertEquals(expected = this.children.subList(14, 14 + 7), actual = select2)

        // Select 3 page of 4
        val select3 = it.select(table = Child::class, page = Page(number = 2, orderBy = Child::pk, limit = 4))
        assertEquals(expected = this.children.subList(8, 8 + 4), actual = select3)

        // It should be consistent
        val select4 = it.select(table = Child::class, page = Page(number = 2, orderBy = Child::pk, limit = 4))
        assertEquals(expected = select3, actual = select4)
    }

    @Test
    fun `test row select pk`() = service.query {
        //Should return expected
        val parent0 = it.select(table = Parent::class, pk = 1)
        assertEquals(expected = this.parents[0], actual = parent0)

        //It should be consistent
        val parent1 = it.select(table = Parent::class, pk = 1)
        assertEquals(expected = parent0, actual = parent1)

        //Should differ
        val parent2 = it.select(table = Parent::class, pk = 2)
        assertEquals(expected = this.parents[1], actual = parent2)
        assertNotEquals(illegal = parent1, actual = parent2)
    }

    @Test
    fun `test row insert`() = service.query {
        //Get current all parents
        val parents = it.select(table = Parent::class)
        assertEquals(expected = this.parents, actual = parents)

        //Create new object
        val newObj = Parent(col = "NEW")

        // does not allready have primary key
        assertEquals(expected = null, actual = newObj.pk)

        //New object is not contained inside parents
        assertFalse(actual = parents.contains(newObj))

        //Insert new object
        assertTrue(actual = it.insert(row = newObj))

        //Check if primary key was updated
        assertTrue(actual = newObj.pk!! > 0)

        //Get parents ad check if new object is contained inside
        val postParents = it.select(table = Parent::class)
        assertTrue(actual = postParents.contains(newObj))

        //Check if table has not been change while inserting
        val preParents = postParents.toMutableList()
        preParents.remove(newObj)
        assertEquals(actual = preParents, expected = parents)

        //Try to insert element again
        assertFalse(actual = it.insert(row = newObj))

        //Parents really stayed as they were before
        val postParents2 = it.select(table = Parent::class)
        assertEquals(actual = postParents2, expected = postParents)
    }

    @Test
    fun `test row update`() = service.query {
        //Get current all parents
        val parents = it.select(table = Parent::class)
        assertEquals(expected = this.parents, actual = parents)

        //Update first
        parents[0].col = "UPDATE"
        assertTrue(it.update(row = parents[0]))

        //Get current all parents
        val postParents = it.select(table = Parent::class)
        assertEquals(expected = parents, actual = postParents)

        //Object should not be updated if has no primary key
        parents[1].pk = null
        parents[1].col = "UPDATE2"
        assertFalse(it.update(row = parents[1]))

        //Update should not change anything in db
        val postParents2 = it.select(table = Parent::class)
        assertEquals(expected = postParents, actual = postParents2)
    }

    @Test
    fun `test row delete`() = service.query {
        //Get current all parents
        val parents = it.select(table = Parent::class)
        assertEquals(expected = this.parents, actual = parents)

        //Check if primary exists and is greater than 0
        assertTrue(parents[0].pk!! > 0)

        //Delete first
        assertTrue(it.delete(row = parents[0]))

        //Check if primary was set to null
        assertEquals(actual = parents[0].pk, expected = null)

        //Get current all parents
        val postParents = it.select(table = Parent::class).toMutableList()

        //Check if the parent was removed
        assertEquals(expected = parents.size, actual = postParents.size + 1)

        //Check if other are as they were before
        val filteredParents = parents.filter { it.pk != null }
        assertEquals(expected = filteredParents, actual = postParents)

        //Object should not be deleted if has no primary key
        parents[1].pk = null
        assertFalse(it.delete(row = parents[1]))

        //Update should not change anything in db
        val postParents2 = it.select(table = Parent::class)
        assertEquals(expected = postParents, actual = postParents2)
    }

    @Test
    fun `test rows insert`() = service.query {

        //Get current all parents
        val parents = it.select(table = Parent::class)
        assertEquals(expected = this.parents, actual = parents)

        //Trying to insert empty array does nothing
        assertEquals(actual = it.insert(rows = listOf()), expected = listOf())

        //State is the same as before
        assertEquals(expected = this.parents, actual = it.select(table = Parent::class))

        //New object is not contained inside parents
        val newObj0 = Parent(col = "NEW0")
        val newObj1 = Parent(col = "NEW1")
        val newObj2 = parents[0]

        assertFalse(actual = parents.contains(newObj0))
        assertFalse(actual = parents.contains(newObj1))
        assertTrue(actual = parents.contains(newObj2))

        //Insert new object
        assertEquals(actual = it.insert(rows = listOf(newObj0, newObj1, newObj2)), expected = listOf(true, true, false))

        //Check if primary key are not updated after batch insert
        assertTrue(actual = newObj0.pk != null)
        assertTrue(actual = newObj1.pk != null)
        assertTrue(actual = newObj2.pk != null)

        //Get updated parents
        val postParents = it.select(table = Parent::class)

        //Check if new parents have 2 more elements because other was allready inserted because of its pk allrady set
        assertEquals(expected = parents.size + 2, actual = postParents.size)

        //Check if new pareents contains those 2 elements
        val last2Parents = postParents.takeLast(2)
        assertEquals(expected = listOf(newObj0, newObj1), actual = last2Parents)

        //Trying to insert elements that are really allready in the database
        assertEquals(actual = it.insert(rows = listOf(newObj0, newObj1, newObj2)), expected = listOf(false, false, false))

        //This will not change anything
        assertEquals(actual = it.select(table = Parent::class), expected = postParents)
    }

    @Test
    fun `test rows update`() = service.query {
        //Get current all parents
        val parents = it.select(table = Parent::class)
        assertEquals(expected = this.parents, actual = parents)

        parents[0].col = "UPDATE0"
        parents[1].col = "UPDATE1"

        //Update
        assertEquals(expected = listOf(true, true), actual = it.update(rows = listOf(parents[0], parents[1])))

        //List should be equal
        val postParents0 = it.select(table = Parent::class)
        assertEquals(expected = parents, actual = postParents0)

        //If you update not allready inserted element it should reject
        assertEquals(expected = listOf(false, false), actual = it.update(rows = listOf(Parent(col = "1"), Parent(col = "r"))))

        //And database should stay the same
        assertEquals(actual = it.select(table = Parent::class), expected = parents)
    }

    @Test
    fun `test rows delete`() = service.query {
        //Get current all parents
        val children = it.select(table = Child::class)
        assertEquals(expected = this.children, actual = children)

        //Delete
        assertEquals(expected = listOf(true, true), actual = it.delete(listOf(children[0], children[1])))

        //Primary keys are not deleted
        assertEquals(actual = children[0].pk, expected = null)
        assertEquals(actual = children[1].pk, expected = null)

        //List should not be equal
        val postChildren0 = it.select(table = Child::class)
        assertEquals(expected = children.size, actual = postChildren0.size + 2)
        val filteredChildren = children.drop(2)
        assertEquals(expected = filteredChildren, actual = postChildren0)

        //If you try to delete not allready inserted element it should reject
        assertEquals(expected = listOf(false, false), actual = it.delete(rows = listOf(Parent(col = "1"), Parent(col = "r"))))

        //And database should stay the same
        assertEquals(actual = it.select(table = Parent::class), expected = parents)
    }


    @Test
    fun `test rows insertBatch`() = service.query {
        //Get current all parents
        val parents = it.select(table = Parent::class)
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
        assertEquals(actual = it.insertBatch(rows = listOf(newObj0, newObj1)), expected = 2)

        //Check if primary key are not updated after batch insert
        assertEquals(expected = null, actual = newObj0.pk)
        assertEquals(expected = null, actual = newObj1.pk)

        val postParents = it.select(table = Parent::class)
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
        val postParents2 = it.select(table = Parent::class)
        assertEquals(actual = it.insertBatch(rows = listOf(newObj0, newObj1)), expected = 0)

        //Parents really stayed as they were before
        val postParents3 = it.select(table = Parent::class)

        //Check if post and pre matches
        assertEquals(actual = postParents2, expected = postParents3)
    }

    @Test
    fun `test rows updateBatch`() = service.query {
        //Get current all parents
        val parents = it.select(table = Parent::class)
        assertEquals(expected = this.parents, actual = parents)

        parents[0].col = "UPDATE0"
        parents[1].col = "UPDATE1"

        //Update
        assertEquals(expected = 2, actual = it.updateBatch(rows = listOf(parents[0], parents[1])))

        //List should be equal
        val postParents0 = it.select(table = Parent::class)
        assertEquals(expected = parents, actual = postParents0)

        //Insert without primary key
        postParents0[2].pk = null
        postParents0[3].pk = null

        //Create snapshot before inserting for comparison
        val postParents1 = it.select(table = Parent::class)
        assertEquals(expected = 0, actual = it.updateBatch(rows = listOf(postParents0[2], postParents0[3])))

        //List should be equal
        val postParents2 = it.select(table = Parent::class)
        assertEquals(expected = postParents1, actual = postParents2)
    }

    @Test
    fun `test rows deleteBatch`() = service.query {
        //Get current all parents
        val children = it.select(table = Child::class)
        assertEquals(expected = this.children, actual = children)

        //Delete
        assertEquals(expected = 2, actual = it.deleteBatch(listOf(children[0], children[1])))

        //Primary keys are not deleted
        assertEquals(actual = children[0].pk, expected = null)
        assertEquals(actual = children[1].pk, expected = null)

        //List should not be equal
        val postChildren0 = it.select(table = Child::class)
        assertEquals(expected = children.size, actual = postChildren0.size + 2)
        val filteredChildren = children.drop(2)
        assertEquals(expected = filteredChildren, actual = postChildren0)

        //Insert without primary key
        postChildren0[2].pk = null
        postChildren0[3].pk = null

        //Create snapshot before inserting for comparison
        val postChildren1 = it.select(table = Child::class)
        assertEquals(expected = 0, actual = it.updateBatch(rows = listOf(postChildren0[2], postChildren0[3])))

        //List should be equal
        val postChildren2 = it.select(table = Child::class)
        assertEquals(expected = postChildren1, actual = postChildren2)
    }

    @Test
    fun `test query`() = service.query {
        it.select(table = Parent::class, pk = 1) ?: throw Exception("It should return something...")
        val preParent2 = it.select(table = Parent::class, pk = 2) ?: throw Exception("It should return something...")

        //Get current all parents
        it.query { "delete from main.Parent where pk = 1;" }

        //Check for deletion
        val postParent2 = it.select(table = Parent::class, pk = 2)
        val postParent1 = it.select(table = Parent::class, pk = 1)

        //Parent 1 should be deleted
        assertEquals(expected = null, actual = postParent1)
        assertEquals(expected = preParent2, actual = postParent2)
    }

    @Test
    fun `test query(outputs)`() = service.query {
        //Get current all parents
        val parent1 = it.select(table = Parent::class, pk = 1) ?: throw Exception("It should return something")
        val parent2 = it.select(table = Parent::class, pk = 2) ?: throw Exception("It should return something")

        val objs = it.query(Parent::class) { "select * from main.Parent where pk < 3;" }

        //If multiple select are not supported then it should return only first select
        assertEquals(expected = listOf(parent1, parent2), actual = objs)

        //Also If multiple results are not supported then it should not delete the 1 parent also
        assertEquals(actual = it.select(table = Parent::class, pk = 1), expected = parent1)
    }

    @Test
    fun `test query(outputs, input)`() = service.query {
        //Get current all parents
        val parent1 = it.select(table = Parent::class, pk = 1) ?: throw Exception("It should return something")
        it.select(table = Parent::class, pk = 2) ?: throw Exception("It should return something")

        //Test pre state
        assertNotEquals(illegal = Parent(pk = 1, col = "XXX"), actual = parent1)

        //Execute update
        val input = Input(child_pk = 1, parent_pk = 2)
        val objs: List<Any>? = it.query(Child::class, input = input) {
            """
                    select *
                    from main.Child C
                    join main.Parent P on C.fk = P.pk
                    where P.pk = ${it.get(Input::parent_pk)}
                """.trimIndent()
        }

        assertEquals(
            actual = objs,
            expected =
            listOf(
                Child(pk = 6, fk = 2, col = "-1350163013"),
                Child(pk = 7, fk = 2, col = "1544682258"),
                Child(pk = 8, fk = 2, col = "-182312124"),
                Child(pk = 9, fk = 2, col = "-1397853422"),
                Child(pk = 10, fk = 2, col = "62774084")
            )
        )
    }
}
