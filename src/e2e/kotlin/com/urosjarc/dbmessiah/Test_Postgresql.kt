package com.urosjarc.dbmessiah

import com.urosjarc.dbmessiah.domain.C
import com.urosjarc.dbmessiah.domain.Page
import com.urosjarc.dbmessiah.domain.Table
import com.urosjarc.dbmessiah.impl.postgresql.PgSchema
import com.urosjarc.dbmessiah.impl.postgresql.PgSerializer
import com.urosjarc.dbmessiah.impl.postgresql.PgService
import com.urosjarc.dbmessiah.serializers.AllTS
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.*
import kotlin.reflect.KClass
import kotlin.test.*


open class Test_Postgresql : Test_Contract {
    open var parents = mutableListOf<Parent>()
    open var children = mutableListOf<Child>()

    companion object {
        private lateinit var service: PgService

        @JvmStatic
        @BeforeAll
        fun init() {
            service = PgService(
                config = Properties().apply {
                    this["jdbcUrl"] = "jdbc:postgresql://localhost:5432/public"
                    this["username"] = "root"
                    this["password"] = "root"
                },
                ser = PgSerializer(
                    schemas = listOf(
                        PgSchema(
                            name = "main", tables = listOf(
                                Table(Parent::pk),
                                Table(
                                    Child::pk, foreignKeys = listOf(
                                        Child::fk to Parent::class
                                    ), constraints = listOf(
                                        Child::fk to listOf(C.CASCADE_DELETE)
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
    override fun prepare() {
        //Reseting tables
        service.autocommit {
            it.run.query { "CREATE SCHEMA IF NOT EXISTS main;" }
            it.table.dropCascade(Child::class)
            it.table.dropCascade(Parent::class)
            it.table.create(Parent::class)
            it.table.create(Child::class)
        }

        val numParents = 5
        val numChildren = 5
        children = mutableListOf()
        parents = mutableListOf()

        //Inserting tables
        service.autocommit {
            repeat(times = numParents) { p ->
                val parent = Parent.get(seed = p)
                parents.add(parent)
                val parentInserted = it.row.insert(row = parent)
                if (parent.pk == null || !parentInserted) throw Exception("Parent was not inserted: $parent")
                repeat(numChildren) { c ->
                    val child = Child.get(fk = parent.pk!!, seed = p * numChildren + c)
                    children.add(child)
                    val childInserted = it.row.insert(row = child)
                    if (child.pk == null || !childInserted) throw Exception("Children was not inserted: $child")
                }
            }

            //Testing current state
            val insertedParents = it.table.select(table = Parent::class)
            val insertedChildren = it.table.select(table = Child::class)

            if (insertedChildren != children || insertedParents != parents)
                throw Exception("Test state does not match with expected state")
        }

        //Create procedures and disable foreign checks
        service.autocommit {
            it.run.query {
                """
                CREATE OR REPLACE FUNCTION main.TestProcedure(parent_pk INT)
                RETURNS SETOF main.Parent AS $$
                BEGIN
                  RETURN QUERY SELECT * FROM main.Parent WHERE pk = parent_pk;
                END;
                $$ language plpgsql;
                """.trimIndent()
            }
            it.run.query {
                """
                CREATE OR REPLACE FUNCTION main.TestProcedureEmpty()
                RETURNS SETOF main.Parent AS $$
                BEGIN
                    RETURN QUERY SELECT * FROM main.Parent WHERE pk = 2;
                END;
                $$ language plpgsql;
                """.trimIndent()
            }
        }

    }

    private fun assertTableNotExists(q: PgService.Connection, kclass: KClass<*>) {
        val e = assertThrows<Throwable> { q.table.select(table = kclass) }
        assertContains(
            charSequence = e.stackTraceToString(),
            other = """relation "main.parent" does not exist""",
            message = e.stackTraceToString()
        )
    }

    @Test
    override fun `test table drop`() = service.autocommit {
        //You can select
        it.table.select(table = Parent::class)

        //Drop
        it.table.dropCascade(table = Parent::class)

        //You can't select on droped table
        this.assertTableNotExists(q = it, kclass = Parent::class)
    }

    @Test
    override fun `test table create`() = service.autocommit {
        //Get pre create state
        val preParents = it.table.select(table = Parent::class)
        assertTrue(actual = preParents.isNotEmpty())

        //Create table if allready created should not throw error
        assertEquals(actual = it.table.create(table = Parent::class), expected = 0)

        //Create table should not change previous state
        val postParents = it.table.select(table = Parent::class)
        assertEquals(actual = postParents, expected = preParents)

        //Drop
        assertEquals(actual = it.table.dropCascade(Parent::class), expected = 0)

        //Select will create error
        this.assertTableNotExists(q = it, kclass = Parent::class)

        //Recreate table
        assertEquals(actual = it.table.create(table = Parent::class), expected = 0)

        //Now we can get elements
        assertTrue(it.table.select(table = Parent::class).isEmpty())
    }

    @Test
    override fun `test table delete`() = service.autocommit {
        //Get current all parents
        val parents = it.table.select(table = Parent::class)
        assertEquals(expected = this.parents, actual = parents)

        //Check if number of deletes matches original size
        assertTrue(parents.isNotEmpty())
        assertEquals(expected = parents.size, actual = it.table.delete(table = Parent::class))

        //Check if no parent is left
        val postParents = it.table.select(table = Parent::class)
        assertTrue(actual = postParents.isEmpty())

        //If again delete return 0
        assertEquals(expected = 0, actual = it.table.delete(table = Parent::class))
    }

    @Test
    override fun `test table select`() = service.autocommit {
        //It should be equal to inserted parents
        val selected0 = it.table.select(table = Parent::class)
        assertEquals(expected = this.parents, actual = selected0)

        //It should be consistent
        val selected1 = it.table.select(table = Parent::class)
        assertEquals(expected = selected0, actual = selected1)
    }

    @Test
    override fun `test table select page`() = service.autocommit {
        // Select first 5
        val select0 = it.table.select(table = Child::class, page = Page(number = 0, orderBy = Child::pk, limit = 5))
        assertEquals(expected = this.children.subList(0, 5), actual = select0)

        // Select first 7
        val select1 = it.table.select(table = Child::class, page = Page(number = 0, orderBy = Child::pk, limit = 7))
        assertEquals(expected = this.children.subList(0, 7), actual = select1)

        // Select 3 page of 7
        val select2 = it.table.select(table = Child::class, page = Page(number = 2, orderBy = Child::pk, limit = 7))
        assertEquals(expected = this.children.subList(14, 14 + 7), actual = select2)

        // Select 3 page of 4
        val select3 = it.table.select(table = Child::class, page = Page(number = 2, orderBy = Child::pk, limit = 4))
        assertEquals(expected = this.children.subList(8, 8 + 4), actual = select3)

        // It should be consistent
        val select4 = it.table.select(table = Child::class, page = Page(number = 2, orderBy = Child::pk, limit = 4))
        assertEquals(expected = select3, actual = select4)
    }

    @Test
    override fun `test row select pk`() = service.autocommit {
        //Should return expected
        val parent0 = it.row.select(table = Parent::class, pk = 1)
        assertEquals(expected = this.parents[0], actual = parent0)

        //It should be consistent
        val parent1 = it.row.select(table = Parent::class, pk = 1)
        assertEquals(expected = parent0, actual = parent1)

        //Should differ
        val parent2 = it.row.select(table = Parent::class, pk = 2)
        assertEquals(expected = this.parents[1], actual = parent2)
        assertNotEquals(illegal = parent1, actual = parent2)
    }

    @Test
    override fun `test row insert`() = service.autocommit {
        //Get current all parents
        val parents = it.table.select(table = Parent::class)
        assertEquals(expected = this.parents, actual = parents)

        //Create new object
        val newObj = Parent(col = "NEW")

        // does not allready have primary key
        assertEquals(expected = null, actual = newObj.pk)

        //New object is not contained inside parents
        assertFalse(actual = parents.contains(newObj))

        //Insert new object
        assertTrue(actual = it.row.insert(row = newObj))

        //Check if primary key was updated
        assertTrue(actual = newObj.pk!! > 0)

        //Get parents ad check if new object is contained inside
        val postParents = it.table.select(table = Parent::class)
        assertTrue(actual = postParents.contains(newObj))

        //Check if table has not been change while inserting
        val preParents = postParents.toMutableList()
        preParents.remove(newObj)
        assertEquals(actual = preParents, expected = parents)

        //Try to insert element again
        assertFalse(actual = it.row.insert(row = newObj))

        //Parents really stayed as they were before
        val postParents2 = it.table.select(table = Parent::class)
        assertEquals(actual = postParents2, expected = postParents)
    }

    @Test
    override fun `test row update`() = service.autocommit {
        //Get current all parents
        val parents = it.table.select(table = Parent::class)
        assertEquals(expected = this.parents, actual = parents)

        //Update first
        parents[0].col = "UPDATE"
        assertTrue(it.row.update(row = parents[0]))

        //Get current all parents
        val postParents = it.table.select(table = Parent::class)
        assertEquals(expected = parents.sortedBy { it.pk }, actual = postParents.sortedBy { it.pk })

        //Object should not be updated if has no primary key
        parents[1].pk = null
        parents[1].col = "UPDATE2"
        assertFalse(it.row.update(row = parents[1]))

        //Update should not change anything in db
        val postParents2 = it.table.select(table = Parent::class)
        assertEquals(expected = postParents, actual = postParents2)
    }

    @Test
    override fun `test row delete`() = service.autocommit {
        //Get current all parents
        val parents = it.table.select(table = Parent::class)
        assertEquals(expected = this.parents, actual = parents)

        //Check if primary exists and is greater than 0
        assertTrue(parents[0].pk!! > 0)

        //Delete first
        assertTrue(it.row.delete(row = parents[0]))

        //Check if primary was set to null
        assertEquals(actual = parents[0].pk, expected = null)

        //Get current all parents
        val postParents = it.table.select(table = Parent::class).toMutableList()

        //Check if the parent was removed
        assertEquals(expected = parents.size, actual = postParents.size + 1)

        //Check if other are as they were before
        val filteredParents = parents.filter { it.pk != null }
        assertEquals(expected = filteredParents, actual = postParents)

        //Object should not be deleted if has no primary key
        parents[1].pk = null
        assertFalse(it.row.delete(row = parents[1]))

        //Update should not change anything in db
        val postParents2 = it.table.select(table = Parent::class)
        assertEquals(expected = postParents, actual = postParents2)
    }

    @Test
    override fun `test rows insert`() = service.autocommit {

        //Get current all parents
        val parents = it.table.select(table = Parent::class)
        assertEquals(expected = this.parents, actual = parents)

        //Trying to insert empty array does nothing
        assertEquals(actual = it.row.insert(rows = listOf()), expected = listOf())

        //State is the same as before
        assertEquals(expected = this.parents, actual = it.table.select(table = Parent::class))

        //New object is not contained inside parents
        val newObj0 = Parent(col = "NEW0")
        val newObj1 = Parent(col = "NEW1")
        val newObj2 = parents[0]

        assertFalse(actual = parents.contains(newObj0))
        assertFalse(actual = parents.contains(newObj1))
        assertTrue(actual = parents.contains(newObj2))

        //Insert new object
        assertEquals(actual = it.row.insert(rows = listOf(newObj0, newObj1, newObj2)), expected = listOf(true, true, false))

        //Check if primary key are not updated after batch insert
        assertTrue(actual = newObj0.pk != null)
        assertTrue(actual = newObj1.pk != null)
        assertTrue(actual = newObj2.pk != null)

        //Get updated parents
        val postParents = it.table.select(table = Parent::class)

        //Check if new parents have 2 more elements because other was allready inserted because of its pk allrady set
        assertEquals(expected = parents.size + 2, actual = postParents.size)

        //Check if new pareents contains those 2 elements
        val last2Parents = postParents.takeLast(2)
        assertEquals(expected = listOf(newObj0, newObj1), actual = last2Parents)

        //Trying to insert elements that are really allready in the database
        assertEquals(actual = it.row.insert(rows = listOf(newObj0, newObj1, newObj2)), expected = listOf(false, false, false))

        //This will not change anything
        assertEquals(actual = it.table.select(table = Parent::class), expected = postParents)
    }

    @Test
    override fun `test rows update`() = service.autocommit {
        //Get current all parents
        val parents = it.table.select(table = Parent::class)
        assertEquals(expected = this.parents, actual = parents)

        parents[0].col = "UPDATE0"
        parents[1].col = "UPDATE1"

        //Update
        assertEquals(expected = listOf(true, true), actual = it.row.update(rows = listOf(parents[0], parents[1])))

        //List should be equal
        val postParents0 = it.table.select(table = Parent::class)
        assertEquals(expected = parents.sortedBy { it.pk }, actual = postParents0.sortedBy { it.pk })

        //If you update not allready inserted element it should reject
        assertEquals(expected = listOf(false, false), actual = it.row.update(rows = listOf(Parent(col = "1"), Parent(col = "r"))))

        //And database should stay the same
        assertEquals(actual = it.table.select(table = Parent::class).sortedBy { it.pk }, expected = parents.sortedBy { it.pk })
    }

    @Test
    override fun `test rows delete`() = service.autocommit {
        //Get current all parents
        val children = it.table.select(table = Child::class)
        assertEquals(expected = this.children, actual = children)

        //Delete
        assertEquals(expected = listOf(true, true), actual = it.row.delete(listOf(children[0], children[1])))

        //Primary keys are not deleted
        assertEquals(actual = children[0].pk, expected = null)
        assertEquals(actual = children[1].pk, expected = null)

        //List should not be equal
        val postChildren0 = it.table.select(table = Child::class)
        assertEquals(expected = children.size, actual = postChildren0.size + 2)
        val filteredChildren = children.drop(2)
        assertEquals(expected = filteredChildren, actual = postChildren0)

        //If you try to delete not allready inserted element it should reject
        assertEquals(expected = listOf(false, false), actual = it.row.delete(rows = listOf(Parent(col = "1"), Parent(col = "r"))))

        //And database should stay the same
        assertEquals(actual = it.table.select(table = Parent::class), expected = parents)
    }


    @Test
    override fun `test rows insertBatch`() = service.autocommit {
        //Get current all parents
        val parents = it.table.select(table = Parent::class)
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
        assertEquals(actual = it.batch.insert(rows = listOf(newObj0, newObj1)), expected = 2)

        //Check if primary key are not updated after batch insert
        assertEquals(expected = null, actual = newObj0.pk)
        assertEquals(expected = null, actual = newObj1.pk)

        val postParents = it.table.select(table = Parent::class)
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
        val postParents2 = it.table.select(table = Parent::class)
        assertEquals(actual = it.batch.insert(rows = listOf(newObj0, newObj1)), expected = 0)

        //Parents really stayed as they were before
        val postParents3 = it.table.select(table = Parent::class)

        //Check if post and pre matches
        assertEquals(actual = postParents2, expected = postParents3)
    }

    @Test
    override fun `test rows updateBatch`() = service.autocommit {
        //Get current all parents
        val parents = it.table.select(table = Parent::class)
        assertEquals(expected = this.parents, actual = parents)

        parents[0].col = "UPDATE0"
        parents[1].col = "UPDATE1"

        //Update
        assertEquals(expected = 2, actual = it.batch.update(rows = listOf(parents[0], parents[1])))

        //List should be equal
        val postParents0 = it.table.select(table = Parent::class)
        assertEquals(expected = parents.sortedBy { it.pk }, actual = postParents0.sortedBy { it.pk })

        //Insert without primary key
        postParents0[2].pk = null
        postParents0[3].pk = null

        //Create snapshot before inserting for comparison
        val postParents1 = it.table.select(table = Parent::class)
        assertEquals(expected = 0, actual = it.batch.update(rows = listOf(postParents0[2], postParents0[3])))

        //List should be equal
        val postParents2 = it.table.select(table = Parent::class)
        assertEquals(expected = postParents1, actual = postParents2)
    }

    @Test
    override fun `test rows deleteBatch`() = service.autocommit {
        //Get current all parents
        val children = it.table.select(table = Child::class)
        assertEquals(expected = this.children, actual = children)

        //Delete
        assertEquals(expected = 2, actual = it.batch.delete(listOf(children[0], children[1])))

        //Primary keys are not deleted
        assertEquals(actual = children[0].pk, expected = null)
        assertEquals(actual = children[1].pk, expected = null)

        //List should not be equal
        val postChildren0 = it.table.select(table = Child::class)
        assertEquals(expected = children.size, actual = postChildren0.size + 2)
        val filteredChildren = children.drop(2)
        assertEquals(expected = filteredChildren, actual = postChildren0)

        //Insert without primary key
        postChildren0[2].pk = null
        postChildren0[3].pk = null

        //Create snapshot before inserting for comparison
        val postChildren1 = it.table.select(table = Child::class)
        assertEquals(expected = 0, actual = it.batch.update(rows = listOf(postChildren0[2], postChildren0[3])))

        //List should be equal
        val postChildren2 = it.table.select(table = Child::class)
        assertEquals(expected = postChildren1, actual = postChildren2)
    }

    @Test
    override fun `test query`() = service.autocommit {
        it.row.select(table = Parent::class, pk = 1) ?: throw Exception("It should return something...")
        it.row.select(table = Parent::class, pk = 2) ?: throw Exception("It should return something...")

        //Get current all parents
        it.run.query {
            """
            delete from main.Parent where pk = 1;
            delete from main.Parent where pk = 2;
            """
        }

        //Check for deletion
        val postParent2 = it.row.select(table = Parent::class, pk = 2)
        val postParent1 = it.row.select(table = Parent::class, pk = 1)

        //Parent 1 should be deleted
        assertEquals(expected = null, actual = postParent1)
        assertEquals(expected = null, actual = postParent2)
    }

    @Test
    override fun `test query(outputs)`() = service.autocommit {
        //Get current all parents
        val parent1 = it.row.select(table = Parent::class, pk = 1) ?: throw Exception("It should return something")
        val parent2 = it.row.select(table = Parent::class, pk = 2) ?: throw Exception("It should return something")

        val objs = it.run.query(Parent::class, Parent::class) {
            """
                    select * from main.Parent where pk < 3;
                    select * from main.Parent where pk = 1;
                    delete from main.Parent where pk = 1;
                """.trimIndent()
        }

        //If multiple select are not supported then it should return only first select
        assertEquals(expected = listOf(listOf(parent1, parent2), listOf(parent1)), actual = objs)

        //Also If multiple results are not supported then it should delete the 1 parent also
        assertEquals(actual = it.row.select(table = Parent::class, pk = 1), expected = null)
    }

    @Test
    override fun `test query(outputs, input)`() = service.autocommit {
        //Get current all parents
        val parent1 = it.row.select(table = Parent::class, pk = 1) ?: throw Exception("It should return something")
        it.row.select(table = Parent::class, pk = 2) ?: throw Exception("It should return something")

        //Test pre state
        assertNotEquals(illegal = Parent(pk = 1, col = "XXX"), actual = parent1)

        //Execute update
        val input = Input(child_pk = 1, parent_pk = 2)
        val objs = it.run.query(Child::class, Child::class, input = input) {
            """
                    select *
                    from main.Child C
                    join main.Parent P on C.fk = P.pk
                    where P.pk = ${it.put(Input::parent_pk)};
                    
                    select *
                    from main.Child C
                    join main.Parent P on C.fk = P.pk
                    where P.pk = ${it.put(Input::parent_pk)}
                """.trimIndent()
        }

        assertEquals(
            actual = objs,
            expected =
            listOf(
                listOf(
                    Child(pk = 6, fk = 2, col = "-1350163013"),
                    Child(pk = 7, fk = 2, col = "1544682258"),
                    Child(pk = 8, fk = 2, col = "-182312124"),
                    Child(pk = 9, fk = 2, col = "-1397853422"),
                    Child(pk = 10, fk = 2, col = "62774084")
                ),
                listOf(
                    Child(pk = 6, fk = 2, col = "-1350163013"),
                    Child(pk = 7, fk = 2, col = "1544682258"),
                    Child(pk = 8, fk = 2, col = "-182312124"),
                    Child(pk = 9, fk = 2, col = "-1397853422"),
                    Child(pk = 10, fk = 2, col = "62774084")
                )
            )
        )
    }


    @Test
    override fun `test transaction with rollback all`() {
        service.transaction {
            //Get state snapshot
            val parents0 = it.table.select(table = Parent::class)
            assertTrue(parents0.isNotEmpty())

            //Delete all table
            it.table.delete(table = Parent::class)

            //Check if table is deleted
            val parents1 = it.table.select(table = Parent::class)
            assertTrue(parents1.isEmpty())

            //Rollback changes
            it.roolback.all()

            //Check if rollback revert changes
            val parents2 = it.table.select(table = Parent::class)
            assertTrue(parents2.isNotEmpty())
            assertEquals(actual = parents2, expected = parents0)
        }
    }

    @Test
    override fun `test transaction with exception`() {
        var parents0: List<Parent> = listOf()
        val e = assertThrows<Throwable> {
            service.transaction {
                //Get state snapshot
                parents0 = it.table.select(table = Parent::class)
                assertTrue(parents0.isNotEmpty())

                //Delete table
                it.table.delete(table = Parent::class)

                //Check if table is really deleted
                val parents1 = it.table.select(table = Parent::class)
                assertTrue(parents1.isEmpty())

                //Raise exception
                throw Throwable()
            }
        }

        assertContains(charSequence = e.stackTraceToString(), "executing rollback")

        //Check if transaction did not finished
        service.autocommit {
            val parents2 = it.table.select(table = Parent::class)
            assertTrue(parents2.isNotEmpty())
            assertEquals(actual = parents2, expected = parents0)
        }
    }

    @Test
    override fun `test transaction with roolback snapshot`() {
        var parents0: List<Parent>
        var children0: List<Child>

        assertThrows<Throwable> {
            service.transaction {
                //Get state snapshot
                parents0 = it.table.select(table = Parent::class)
                children0 = it.table.select(table = Child::class)

                //Check if both tables are filled
                assertTrue(parents0.isNotEmpty())
                assertTrue(parents0.isNotEmpty())

                val save0 = it.roolback.savePoint()

                //Delete child table
                it.table.delete(table = Child::class)

                //Save point
                val save1 = it.roolback.savePoint()

                //Delete parent table
                it.table.delete(table = Parent::class)

                //Get final state of the system
                val parents1 = it.table.select(table = Parent::class)
                val children1 = it.table.select(table = Child::class)
                assertTrue(parents1.isEmpty())
                assertTrue(children1.isEmpty())

                it.roolback.to(point = save1)

                //Get roolback state 1 snapshot
                val parents2 = it.table.select(table = Parent::class)
                val children2 = it.table.select(table = Child::class)
                assertTrue(parents2.isEmpty())
                assertEquals(actual = children2, expected = children0)

                it.roolback.to(point = save0)

                //Get roolback state 1 snapshot
                val parents3 = it.table.select(table = Parent::class)
                val children3 = it.table.select(table = Child::class)
                assertEquals(actual = parents3, expected = parents0)
                assertEquals(actual = children3, expected = children0)
            }
        }
    }
}
