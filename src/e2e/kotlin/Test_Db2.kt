import com.urosjarc.dbmessiah.domain.C
import com.urosjarc.dbmessiah.domain.Page
import com.urosjarc.dbmessiah.domain.Table
import com.urosjarc.dbmessiah.exceptions.QueryException
import com.urosjarc.dbmessiah.impl.db2.Db2Schema
import com.urosjarc.dbmessiah.impl.db2.Db2Serializer
import com.urosjarc.dbmessiah.impl.db2.Db2Service
import com.urosjarc.dbmessiah.serializers.AllTS
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.*
import kotlin.test.*


open class Test_Db2 : Test_Contract {
    open var parents = mutableListOf<Parent>()
    open var children = mutableListOf<Child>()

    companion object {
        private lateinit var service: Db2Service

        val schema = Db2Schema(
            name = "main", tables = listOf(
                Table(Parent::pk),
                Table(
                    Child::pk, foreignKeys = listOf(
                        Child::fk to Parent::class
                    ), constraints = listOf(
                        Child::fk to listOf(C.CASCADE_DELETE)
                    )
                ),
                Table(UUIDParent::pk)
            ),
            procedures = listOf(
                TestProcedure::class,
                TestProcedureEmpty::class
            )
        )

        @JvmStatic
        @BeforeAll
        fun init() {
            service = Db2Service(
                config = Properties().apply {
                    this["jdbcUrl"] = "jdbc:db2://localhost:50000/main"
                    this["username"] = "db2inst1"
                    this["password"] = "root"
                },
                ser = Db2Serializer(
                    schemas = listOf(schema),
                    globalSerializers = AllTS.db2,
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
            it.schema.create(schema = schema, throws = false)
            it.table.drop<Child>()
            it.table.drop<Parent>()
            it.table.drop<UUIDParent>()
            it.table.create<Parent>()
            it.table.create<Child>()
            it.table.create<UUIDParent>()
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
                it.row.insert(row = parent)
                if (parent.pk == null) throw Exception("Parent was not inserted: $parent")
                repeat(numChildren) { c ->
                    val child = Child.get(fk = parent.pk!!, seed = p * numChildren + c)
                    children.add(child)
                    it.row.insert(row = child)
                    if (child.pk == null) throw Exception("Children was not inserted: $child")
                }
            }

            //Testing current state
            val insertedParents = it.table.select<Parent>()
            val insertedChildren = it.table.select<Child>()

            if (insertedChildren != children || insertedParents != parents)
                throw Exception("Test state does not match with expected state")
        }

        //Create procedures and disable foreign checks
        service.autocommit {
            it.procedure.drop<TestProcedureEmpty>(throws = false)
            it.procedure.create<TestProcedureEmpty> {
                """
                    INSERT INTO ${it.table<Parent>()} 
                        (${it.name(Parent::pk)}, ${it.name(Parent::col)})
                    VALUES
                        (1234, 'new parent from procedure');
                """
            }
            it.procedure.drop<TestProcedure>(throws = false)
            it.procedure.create<TestProcedure> {
                """
                    INSERT INTO ${it.table<Parent>()}
                        (${it.name(Parent::pk)}, ${it.name(Parent::col)})
                    VALUES
                        (${it.arg(TestProcedure::parent_pk)}, ${it.arg(TestProcedure::parent_col)});
                """
            }
        }
    }

    private inline fun <reified T : Any> assertTableNotExists(q: Db2Service.Connection) {
        val e = assertThrows<Throwable> { q.table.select<T>() }
        assertContains(
            charSequence = e.stackTraceToString(),
            other = """SQLERRMC=main.Parent""",
            message = e.stackTraceToString()
        )
    }

    @Test
    override fun `test table drop`() = service.autocommit {
        //You can select
        it.table.select<Parent>()

        //Drop
        it.table.drop<Parent>()

        //You can't select on droped table
        this.assertTableNotExists<Parent>(q = it)
    }

    @Test
    override fun `test table create`() = service.autocommit {
        //Get pre create state
        val preParents = it.table.select<Parent>()
        assertTrue(actual = preParents.isNotEmpty())

        //Create table if allready created should not throw error
        assertEquals(actual = it.table.create<Parent>(), expected = 0)

        //Create table should not change previous state
        val postParents = it.table.select<Parent>()
        assertEquals(actual = postParents, expected = preParents)

        //Drop
        assertEquals(actual = it.table.drop<Parent>(), expected = 0)

        //Select will create error
        this.assertTableNotExists<Parent>(q = it)

        //Recreate table
        assertEquals(actual = it.table.create<Parent>(), expected = 0)

        //Now we can get elements
        assertTrue(it.table.select<Parent>().isEmpty())
    }

    @Test
    override fun `test table delete`() = service.autocommit {
        //Get current all parents
        val parents = it.table.select<Parent>()
        assertEquals(expected = this.parents, actual = parents)

        //Check if number of deletes matches original size
        assertTrue(parents.isNotEmpty())
        assertEquals(expected = parents.size, actual = it.table.delete<Parent>())

        //Check if no parent is left
        val postParents = it.table.select<Parent>()
        assertTrue(actual = postParents.isEmpty())

        //If again delete return 0
        assertEquals(expected = 0, actual = it.table.delete<Parent>())
    }

    @Test
    override fun `test table select`() = service.autocommit {
        //It should be equal to inserted parents
        val selected0 = it.table.select<Parent>()
        assertEquals(expected = this.parents, actual = selected0)

        //It should be consistent
        val selected1 = it.table.select<Parent>()
        assertEquals(expected = selected0, actual = selected1)
    }

    @Test
    override fun `test table select page`() = service.autocommit {
        // Select first 5
        val select0 = it.table.select<Child>(page = Page(number = 0, orderBy = Child::pk, limit = 5))
        assertEquals(expected = this.children.subList(0, 5), actual = select0)

        // Select first 7
        val select1 = it.table.select<Child>(page = Page(number = 0, orderBy = Child::pk, limit = 7))
        assertEquals(expected = this.children.subList(0, 7), actual = select1)

        // Select 3 page of 7
        val select2 = it.table.select<Child>(page = Page(number = 2, orderBy = Child::pk, limit = 7))
        assertEquals(expected = this.children.subList(14, 14 + 7), actual = select2)

        // Select 3 page of 4
        val select3 = it.table.select<Child>(page = Page(number = 2, orderBy = Child::pk, limit = 4))
        assertEquals(expected = this.children.subList(8, 8 + 4), actual = select3)

        // It should be consistent
        val select4 = it.table.select<Child>(page = Page(number = 2, orderBy = Child::pk, limit = 4))
        assertEquals(expected = select3, actual = select4)
    }

    @Test
    override fun `test row select pk`() = service.autocommit {
        //Should return expected
        val parent0 = it.row.select<Parent>(pk = 1)
        assertEquals(expected = this.parents[0], actual = parent0)

        //It should be consistent
        val parent1 = it.row.select<Parent>(pk = 1)
        assertEquals(expected = parent0, actual = parent1)

        //Should differ
        val parent2 = it.row.select<Parent>(pk = 2)
        assertEquals(expected = this.parents[1], actual = parent2)
        assertNotEquals(illegal = parent1, actual = parent2)
    }

    @Test
    override fun `test row insert`() = service.autocommit {
        //Get current all parents
        val parents = it.table.select<Parent>()
        assertEquals(expected = this.parents, actual = parents)

        //Create new object
        val newObj = Parent(col = "NEW")

        // does not allready have primary key
        assertEquals(expected = null, actual = newObj.pk)

        //New object is not contained inside parents
        assertFalse(actual = parents.contains(newObj))

        //Insert new object
        it.row.insert(row = newObj)

        //Check if primary key was updated
        assertTrue(actual = newObj.pk!! > 0)

        //Get parents ad check if new object is contained inside
        val postParents = it.table.select<Parent>()
        assertTrue(actual = postParents.contains(newObj))

        //Check if table has not been change while inserting
        val preParents = postParents.toMutableList()
        preParents.remove(newObj)
        assertEquals(actual = preParents, expected = parents)

        //Try to insert element again
        val e = assertThrows<QueryException> {
            it.row.insert(row = newObj)
        }
        assertContains(charSequence = e.stackTraceToString(), "Row with already defined auto-generated primary key are not allowed to be inserted")

        //Parents really stayed as they were before
        val postParents2 = it.table.select<Parent>()
        assertEquals(actual = postParents2, expected = postParents)
    }

    @Test
    override fun `test row update`() = service.autocommit {
        //Get current all parents
        val parents = it.table.select<Parent>()
        assertEquals(expected = this.parents, actual = parents)

        //Update first
        parents[0].col = "UPDATE"
        it.row.update(row = parents[0])

        //Get current all parents
        val postParents = it.table.select<Parent>()
        assertEquals(expected = parents.sortedBy { it.pk }, actual = postParents.sortedBy { it.pk })

        //Object should not be updated if has no primary key
        parents[1].pk = null
        parents[1].col = "UPDATE2"
        val e = assertThrows<QueryException> {
            it.row.update(row = parents[1])
        }
        assertContains(charSequence = e.stackTraceToString(), other = "Row can't be updated with undefined primary key value")

        //Update should not change anything in db
        val postParents2 = it.table.select<Parent>()
        assertEquals(expected = postParents, actual = postParents2)
    }

    @Test
    override fun `test row delete`() = service.autocommit {
        //Get current all parents
        val parents = it.table.select<Parent>()
        assertEquals(expected = this.parents, actual = parents)

        //Check if primary exists and is greater than 0
        assertTrue(parents[0].pk!! > 0)

        //Delete first
        it.row.delete(row = parents[0])

        //Check if primary was set to null
        assertEquals(actual = parents[0].pk, expected = null)

        //Get current all parents
        val postParents = it.table.select<Parent>().toMutableList()

        //Check if the parent was removed
        assertEquals(expected = parents.size, actual = postParents.size + 1)

        //Check if other are as they were before
        val filteredParents = parents.filter { it.pk != null }
        assertEquals(expected = filteredParents, actual = postParents)

        //Object should not be deleted if has no primary key
        parents[1].pk = null
        val e = assertThrows<QueryException> {
            it.row.delete(row = parents[1])
        }
        assertContains(charSequence = e.stackTraceToString(), other = "Row can't be deleted with undefined primary key value")

        //Update should not change anything in db
        val postParents2 = it.table.select<Parent>()
        assertEquals(expected = postParents, actual = postParents2)
    }

    @Test
    override fun `test rows insert`() = service.autocommit {

        //Get current all parents
        val parents = it.table.select<Parent>()
        assertEquals(expected = this.parents, actual = parents)

        //Trying to insert empty array does nothing
        it.row.insert(rows = listOf())

        //State is the same as before
        assertEquals(expected = this.parents, actual = it.table.select<Parent>())

        //New object is not contained inside parents
        val newObj0 = Parent(col = "NEW0")
        val newObj1 = Parent(col = "NEW1")

        assertFalse(actual = parents.contains(newObj0))
        assertFalse(actual = parents.contains(newObj1))

        //Insert new object
        it.row.insert(rows = listOf(newObj0, newObj1))

        //Check if primary key are not updated after batch insert
        assertTrue(actual = newObj0.pk != null)
        assertTrue(actual = newObj1.pk != null)

        //Get updated parents
        val postParents = it.table.select<Parent>()

        //Check if new parents have 2 more elements because other was allready inserted because of its pk allrady set
        assertEquals(expected = parents.size + 2, actual = postParents.size)

        //Check if new pareents contains those 2 elements
        val last2Parents = postParents.takeLast(2)
        assertEquals(expected = listOf(newObj0, newObj1), actual = last2Parents)

        //Trying to insert elements that are really allready in the database
        val e = assertThrows<QueryException> {
            it.row.insert(rows = listOf(newObj0, newObj1))
        }
        assertContains(
            charSequence = e.stackTraceToString(),
            other = "Row with already defined auto-generated primary key are not allowed to be inserted"
        )

        //This will not change anything
        assertEquals(actual = it.table.select<Parent>(), expected = postParents)
    }

    @Test
    override fun `test rows update`() = service.autocommit {
        //Get current all parents
        val parents = it.table.select<Parent>()
        assertEquals(expected = this.parents, actual = parents)

        parents[0].col = "UPDATE0"
        parents[1].col = "UPDATE1"

        //Update
        it.row.update(rows = listOf(parents[0], parents[1]))

        //List should be equal
        val postParents0 = it.table.select<Parent>()
        assertEquals(expected = parents.sortedBy { it.pk }, actual = postParents0.sortedBy { it.pk })

        //If you update not allready inserted element it should reject
        val e = assertThrows<QueryException> {
            it.row.update(rows = listOf(Parent(col = "1"), Parent(col = "r")))
        }
        assertContains(charSequence = e.stackTraceToString(), other = "Row can't be updated with undefined primary key value")

        //And database should stay the same
        assertEquals(actual = it.table.select<Parent>(), expected = parents)
    }

    @Test
    override fun `test rows delete`() = service.autocommit {
        //Get current all parents
        val children = it.table.select<Child>()
        assertEquals(expected = this.children, actual = children)

        //Delete
        it.row.delete(listOf(children[0], children[1]))

        //Primary keys are not deleted
        assertEquals(actual = children[0].pk, expected = null)
        assertEquals(actual = children[1].pk, expected = null)

        //List should not be equal
        val postChildren0 = it.table.select<Child>()
        assertEquals(expected = children.size, actual = postChildren0.size + 2)
        val filteredChildren = children.drop(2)
        assertEquals(expected = filteredChildren, actual = postChildren0)

        //If you try to delete not allready inserted element it should reject
        val e = assertThrows<QueryException> {
            it.row.delete(rows = listOf(Parent(col = "1"), Parent(col = "r")))
        }
        assertContains(charSequence = e.stackTraceToString(), other = "Row can't be deleted with undefined primary key value")

        //And database should stay the same
        assertEquals(actual = it.table.select<Parent>(), expected = parents)
    }


    @Test
    override fun `test rows insertBatch`() = service.autocommit {
        //Get current all parents
        val parents = it.table.select<Parent>()
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
        it.batch.insert(rows = listOf(newObj0, newObj1))

        //Check if primary key are not updated after batch insert
        assertEquals(expected = null, actual = newObj0.pk)
        assertEquals(expected = null, actual = newObj1.pk)

        val postParents = it.table.select<Parent>()
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
        val postParents2 = it.table.select<Parent>()
        val e = assertThrows<QueryException> {
            it.batch.insert(rows = listOf(newObj0, newObj1))
        }
        assertContains(
            charSequence = e.stackTraceToString(),
            other = "Batched row on index '0', with already defined auto-generated primary key, is not allowed to be inserted"
        )

        //Parents really stayed as they were before
        val postParents3 = it.table.select<Parent>()

        //Check if post and pre matches
        assertEquals(actual = postParents2, expected = postParents3)
    }

    @Test
    override fun `test rows updateBatch`() = service.autocommit {
        //Get current all parents
        val parents = it.table.select<Parent>()
        assertEquals(expected = this.parents, actual = parents)

        parents[0].col = "UPDATE0"
        parents[1].col = "UPDATE1"

        //Update
        it.batch.update(rows = listOf(parents[0], parents[1]))

        //List should be equal
        val postParents0 = it.table.select<Parent>()
        assertEquals(expected = parents.sortedBy { it.pk }, actual = postParents0.sortedBy { it.pk })

        //Insert without primary key
        postParents0[2].pk = null
        postParents0[3].pk = null

        //Create snapshot before inserting for comparison
        val postParents1 = it.table.select<Parent>()
        val e = assertThrows<QueryException> {
            it.batch.update(rows = listOf(postParents0[2], postParents0[3]))
        }
        assertContains(charSequence = e.stackTraceToString(), other = "Batched row on index '0', can't be updated with undefined primary key value")

        //List should be equal
        val postParents2 = it.table.select<Parent>()
        assertEquals(expected = postParents1, actual = postParents2)
    }

    @Test
    override fun `test rows deleteBatch`() = service.autocommit {
        //Get current all parents
        val children = it.table.select<Child>()
        assertEquals(expected = this.children, actual = children)

        //Delete
        it.batch.delete(listOf(children[0], children[1]))

        //Primary keys are not deleted
        assertEquals(actual = children[0].pk, expected = null)
        assertEquals(actual = children[1].pk, expected = null)

        //List should not be equal
        val postChildren0 = it.table.select<Child>()
        assertEquals(expected = children.size, actual = postChildren0.size + 2)
        val filteredChildren = children.drop(2)
        assertEquals(expected = filteredChildren, actual = postChildren0)

        //Insert without primary key
        postChildren0[2].pk = null
        postChildren0[3].pk = null

        //Create snapshot before inserting for comparison
        val postChildren1 = it.table.select<Child>()
        val e = assertThrows<QueryException> {
            it.batch.delete(rows = listOf(postChildren0[2], postChildren0[3]))
        }
        assertContains(charSequence = e.stackTraceToString(), other = "Batched row on index '0', can't be deleted with undefined primary key value")

        //List should be equal
        val postChildren2 = it.table.select<Child>()
        assertEquals(expected = postChildren1, actual = postChildren2)
    }

    @Test
    override fun `test query`() = service.autocommit {
        it.row.select<Parent>(pk = 1) ?: throw Exception("It should return something...")
        val preParent2 = it.row.select<Parent>(pk = 2) ?: throw Exception("It should return something...")

        //Get current all parents
        it.query.run { """${it.DELETE<Parent>()} where ${it.column(Parent::pk)} = 1""" }

        //Check for deletion
        val postParent2 = it.row.select<Parent>(pk = 2)
        val postParent1 = it.row.select<Parent>(pk = 1)

        //Parent 1 should be deleted
        assertEquals(expected = null, actual = postParent1)
        assertEquals(expected = preParent2, actual = postParent2)
    }

    @Test
    override fun `test query(outputs)`() = service.autocommit {
        //Get current all parents
        val parent1 = it.row.select<Parent>(pk = 1) ?: throw Exception("It should return something")
        val parent2 = it.row.select<Parent>(pk = 2) ?: throw Exception("It should return something")

        val objs = it.query.get<Parent> { """${it.SELECT<Parent>()} where ${it.column(Parent::pk)} < 3""" }

        //If multiple select are not supported then it should return only first select
        assertEquals(expected = listOf(parent1, parent2), actual = objs)

        //Also If multiple results are not supported then it should not delete the 1 parent also
        assertEquals(actual = it.row.select<Parent>(pk = 1), expected = parent1)
    }

    @Test
    override fun `test query(outputs, input)`() = service.autocommit {
        //Get current all parents
        val parent1 = it.row.select<Parent>(pk = 1) ?: throw Exception("It should return something")
        it.row.select<Parent>(pk = 2) ?: throw Exception("It should return something")

        //Test pre state
        assertNotEquals(illegal = Parent(pk = 1, col = "XXX"), actual = parent1)

        //Execute update
        val input = Input(child_pk = 1, parent_pk = 2)
        val objs = it.query.get(Child::class, input = input) {
            """
                ${it.SELECT<Child>()} 
                join ${it.table<Parent>()} on ${it.column(Child::fk)} = ${it.column(Parent::pk)}
                where ${it.column(Parent::pk)} = ${it.input(Input::parent_pk)}
            """
        }

        assertEquals(
            actual = objs,
            expected = listOf(
                Child(pk = 6, fk = 2, col = "-1350163013"),
                Child(pk = 7, fk = 2, col = "1544682258"),
                Child(pk = 8, fk = 2, col = "-182312124"),
                Child(pk = 9, fk = 2, col = "-1397853422"),
                Child(pk = 10, fk = 2, col = "62774084")
            )
        )
    }


    @Test
    override fun `test transaction with rollback all`() {
        service.transaction {
            //Get state snapshot
            val parents0 = it.table.select<Parent>()
            assertTrue(parents0.isNotEmpty())

            //Delete all table
            it.table.delete<Parent>()

            //Check if table is deleted
            val parents1 = it.table.select<Parent>()
            assertTrue(parents1.isEmpty())

            //Rollback changes
            it.roolback.all()

            //Check if rollback revert changes
            val parents2 = it.table.select<Parent>()
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
                parents0 = it.table.select<Parent>()
                assertTrue(parents0.isNotEmpty())

                //Delete table
                it.table.delete<Parent>()

                //Check if table is really deleted
                val parents1 = it.table.select<Parent>()
                assertTrue(parents1.isEmpty())

                //Raise exception
                throw Throwable()
            }
        }

        assertContains(charSequence = e.stackTraceToString(), "executing rollback")

        //Check if transaction did not finished
        service.autocommit {
            val parents2 = it.table.select<Parent>()
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
                parents0 = it.table.select<Parent>()
                children0 = it.table.select<Child>()

                //Check if both tables are filled
                assertTrue(parents0.isNotEmpty())
                assertTrue(parents0.isNotEmpty())

                val save0 = it.roolback.savePoint()

                //Delete child table
                it.table.delete<Child>()

                //Save point
                val save1 = it.roolback.savePoint()

                //Delete parent table
                it.table.delete<Parent>()

                //Get final state of the system
                val parents1 = it.table.select<Parent>()
                val children1 = it.table.select<Child>()
                assertTrue(parents1.isEmpty())
                assertTrue(children1.isEmpty())

                it.roolback.to(point = save1)

                //Get roolback state 1 snapshot
                val parents2 = it.table.select<Parent>()
                val children2 = it.table.select<Child>()
                assertTrue(parents2.isEmpty())
                assertEquals(actual = children2, expected = children0)

                it.roolback.to(point = save0)

                //Get roolback state 1 snapshot
                val parents3 = it.table.select<Parent>()
                val children3 = it.table.select<Child>()
                assertEquals(actual = parents3, expected = parents0)
                assertEquals(actual = children3, expected = children0)
            }
        }
    }

    @Test
    override fun `test procedure call without input`() = service.autocommit {
        val parent = Parent(pk = 1234, col = "new parent from procedure")
        assertEquals(expected = null, actual = it.row.select<Parent>(pk = parent.pk!!))
        it.procedure.call(procedure = TestProcedureEmpty())
        assertEquals(expected = parent, actual = it.row.select<Parent>(pk = parent.pk!!))
    }

    @Test
    override fun `test procedure call with input`() = service.autocommit {
        val parent = Parent.get(pk = 1000, seed = 0)
        assertEquals(expected = null, actual = it.row.select<Parent>(pk = parent.pk!!))
        it.procedure.call(procedure = TestProcedure(parent_pk = parent.pk!!, parent_col = parent.col))
        assertEquals(expected = parent, actual = it.row.select<Parent>(pk = parent.pk!!))
    }

    @Test
    override fun `test UUID`() = service.autocommit {
        val uuidParent0 = UUIDParent(col = "col0")
        it.row.insert(uuidParent0)

        val uuidParent1: UUIDParent = it.row.select<UUIDParent>(pk = uuidParent0.pk)!!
        assertEquals(actual = uuidParent1, expected = uuidParent0)

        val uuidParents0 = it.table.select<UUIDParent>()
        assertEquals(actual = uuidParents0, expected = listOf(uuidParent0))
    }
}
