package com.urosjarc.dbmessiah.tests

import com.urosjarc.dbmessiah.DbMessiahService
import com.urosjarc.dbmessiah.domain.queries.Page
import com.urosjarc.dbmessiah.domain.test.TestInput
import com.urosjarc.dbmessiah.domain.test.TestOutput
import com.urosjarc.dbmessiah.domain.test.TestTable
import com.urosjarc.dbmessiah.domain.test.TestTableParent
import com.urosjarc.dbmessiah.exceptions.TesterException
import org.apache.logging.log4j.kotlin.logger

class TestService(val service: DbMessiahService) {

    val log = this.logger()

    /**
     * TESTING
     */

    fun test_crud_cycle() {
        /**
         * DROP
         */
        this.test_drop_children()
        this.test_drop_parent()

        /**
         * CREATE
         */
        this.test_create_parent()
        this.test_create_children()

        /**
         * INSERT
         */
        val parents = this.test_insert_parents()
        this.test_select_parents(parents)

        val children = this.test_insert_children(parents)
        this.test_select_children(children)

        /**
         * SELECT
         */
        //ONE
        this.test_select_oneParent(parents[0].id!!, parents[0])
        this.test_select_oneChildren(children[0].id!!, children[0])

        //batch insert
        this.test_parent_batch_insert()

        //select page
        this.assert(listOf(1, 2, 3, 4, 5) == this.test_select_parentPage(Page(number = 0, orderBy = TestTableParent::id, limit = 5)).map { it.id })
        this.assert(listOf(7, 8, 9, 10, 11, 12) == this.test_select_parentPage(Page(number = 1, orderBy = TestTableParent::id, limit = 6)).map { it.id })
        this.assert(listOf(15, 16, 17, 18, 19, 20, 21) == this.test_select_parentPage(Page(number = 2, orderBy = TestTableParent::id, limit = 7)).map { it.id })
        this.assert(listOf(19, 20, 21, 22) == this.test_select_parentPage(Page(number = 3, orderBy = TestTableParent::id, limit = 6)).map { it.id })

        //Test batch insert on children
        this.test_child_batch_insert()

        //Testing join query
        this.test_query()
        this.test_join_query()
        this.test_output_join_query()
        this.test_input_output_join_query()

        //Update query
        this.test_update()
        this.test_delete_children()

        //Batch update
        this.test_batch_update()

        //Transaction update
        this.test_transaction_rollback()
        this.test_transaction_savepoint()

        //Delete in batch
        this.test_delete_batch()

        //Delete all table
        this.test_delete_all_children()
        this.test_delete_all_parents()

        //Cleaning
        this.test_drop_children()
        this.test_drop_parent()
    }

    private fun test_delete_batch() = service.query {
        val preAll = it.select(TestTable::class)
        assert(preAll.size == 7, preAll.size.toString())

        val count = it.deleteBatch(preAll[0], preAll[1], preAll[2])
        assert(count == 3, count.toString())

        val postAll = it.select(TestTable::class)
        assert(postAll.size == 4, postAll.size.toString())
    }


    private fun test_query() = service.query {
        val preAll = it.select(TestTableParent::class)
        val count = it.query {
            """
                insert into TestTableParent (col13) values ('QUERY');
            """.trimIndent()
        }
        assert(count == 1)
        val postAll = it.select(TestTableParent::class)

        assert(postAll.size == preAll.size + 1)
        val obj = postAll.first { it.col13 == "QUERY" }
        it.delete(obj)

        val postAll2 = it.select(TestTableParent::class)
        assert(postAll2 == preAll)
    }

    private fun test_delete_all_parents() = service.transaction {
        val preAll = it.select(kclass = TestTableParent::class)
        assert(preAll.size == 21)
        val count = it.delete(TestTableParent::class)
        assert(count == 21)
        val postAll = it.select(kclass = TestTableParent::class)
        assert(postAll.size == 0)
    }

    private fun test_delete_all_children() = service.query {
        val preAll = it.select(kclass = TestTable::class)
        assert(preAll.size == 4, preAll.size.toString())
        val count = it.delete(TestTable::class)
        assert(count == 4, count.toString())
        val postAll = it.select(kclass = TestTable::class)
        assert(postAll.size == 0)
    }


    fun test_crud_cycle(numCycles: Int) {
        repeat(numCycles) {
            this.log.warn("Testing cycle: $it")
            this.test_crud_cycle()
        }
    }

    private fun assert(value: Boolean, msg: String? = null) {
        if (value != true) throw TesterException(msg ?: "Service test failed!")
    }

    /**
     * DROP
     */
    fun test_drop_parent() = service.query {
        it.drop(TestTableParent::class)
        try {
            it.select(TestTableParent::class)
        } catch (e: Throwable) {
            return@query
        }
        throw TesterException("Table should not exist!")
    }

    fun test_drop_children() = service.transaction {
        it.drop(TestTable::class)
        try {
            it.select(TestTable::class)
        } catch (e: Throwable) {
            return@transaction
        }
        throw TesterException("Table should not exist!")
    }

    /**
     * CREATE
     */
    fun test_create_parent() = service.transaction {
        it.create(kclass = TestTableParent::class)
        this.assert(it.select(kclass = TestTableParent::class).isEmpty())
    }

    fun test_create_children() = service.query {
        it.create(kclass = TestTable::class)
        this.assert(it.select(kclass = TestTable::class).isEmpty())
    }

    /**
     * INSERT
     */
    fun test_insert_parents(): List<TestTableParent> = service.query {
        val par0 = TestTableParent()
        val par1 = par0.copy(col13 = "XXX")

        //Create copies of elements on how they should look like in the future
        val par0_copy = par0.copy(id = 1)
        val par1_copy = par1.copy(id = 2)

        this.assert(par0.id == null)
        this.assert(par1.id == null)

        this.assert(par0 != par1)

        this.assert(it.insert(obj = par0))
        this.assert(it.insert(obj = par1))

        //Test elements and compare it with future copies
        this.assert(par0.id == 1 && par0 == par0_copy)
        this.assert(par1.id == 2 && par1 == par1_copy)

        return@query listOf(par0, par1)
    }

    fun test_insert_children(list: List<TestTableParent>): List<TestTable> = service.transaction {
        val child0 = TestTable(col12 = "XXX", parent_id = list[0].id)
        val child1 = child0.copy(col12 = "YYY", parent_id = list[1].id)
        val child2 = child0.copy(col12 = "ZZZ", parent_id = list[1].id)
        //Create copies of elements on how they should look like in the future
        val child0_copy = child0.copy(id = 1)
        val child1_copy = child1.copy(id = 2)
        val child2_copy = child2.copy(id = 3)

        this.assert(child0.id == null)
        this.assert(child1.id == null)
        this.assert(child2.id == null)

        this.assert(child0 != child1 && child0 != child1 && child2 != child1)

        this.assert(it.insert(obj = child0))
        this.assert(it.insert(obj = child1))
        this.assert(it.insert(obj = child2))

        //Test elements and compare it with future copies
        this.assert(child0.id == 1 && child0_copy == child0)
        this.assert(child1.id == 2 && child1_copy == child1)
        this.assert(child2.id == 3 && child2_copy == child2)

        return@transaction listOf(child0, child1, child2)
    }

    fun test_child_batch_insert() = service.query {
        val batch = mutableListOf<TestTable>()
        repeat(5) { num -> batch.add(TestTable(parent_id = num, col12 = "child_$num")) }
        val count = it.insertBatch(*batch.toTypedArray())
        assert(5 == count, count.toString())
    }

    fun test_parent_batch_insert() = service.transaction {
        val batch = mutableListOf<TestTableParent>()
        repeat(20) { num -> batch.add(TestTableParent(col13 = "copy $num")) }
        val count = it.insertBatch(*batch.toTypedArray())
        assert(20 == count, count.toString())
    }

    /**
     * UPDATE
     */
    fun test_update() = service.query {
        val preChildren = it.select(TestTable::class).toMutableList()

        //Test child
        val preChild = it.select(TestTable::class, pk = 1) ?: throw TesterException("Selected child should not be null")
        val preChildSnapshot = preChild.copy()
        assert(preChildren.contains(preChild))
        assert(preChild.col12 == "XXX", msg = preChild.col12)
        preChild.col12 = "xxx"

        val futureChild = preChild.copy(col12 = "xxx")

        //Update child
        it.update(obj = preChild)

        val afterUpdateChildren = it.select(TestTable::class)
        assert(afterUpdateChildren.contains(preChild))

        //Test child
        val postChildren = it.select(TestTable::class).toMutableList()
        val postChild = it.select(TestTable::class, pk = 1) ?: throw TesterException("Selected child should not be null")
        assert(postChild == preChild)
        assert(postChild == futureChild)
        assert(postChildren.contains(postChild))

        //Test if no other children has changed
        postChildren.remove(postChild)
        preChildren.remove(preChildSnapshot)
        assert(postChildren == preChildren)
    }

    private fun test_batch_update() = service.transaction {
        val preAll = it.select(TestTable::class)
        preAll.forEach { tt ->
            assert(tt.id != null)
            tt.col12 = "BATCH UPDATE"
        }
        assert(preAll.size > 0)
        val count = it.updateBatch(*preAll.toTypedArray())
        assert(preAll.count() == count, count.toString())

        val postAll = it.select(TestTable::class)
        assert(preAll == postAll)
    }

    /**
     * DELETE
     */
    fun test_delete_children() = service.query {
        val preChildren = it.select(TestTable::class).toMutableList()
        val preChild = it.select(TestTable::class, 1) ?: throw TesterException("Selected child should not be null")
        assert(preChildren.contains(preChild))

        assert(preChild.id != null)
        it.delete(preChild)
        assert(preChild.id == null)

        val postChildren = it.select(TestTable::class)
        val postChild = it.select(TestTable::class, 1)

        assert(postChild == null)
        assert(!postChildren.contains(postChild))

        preChildren.removeIf { it.id == 1 }

        assert(preChildren == postChildren)
    }

    /**
     * SELECT
     */
    fun test_select_parents(list: List<TestTableParent>): List<TestTableParent> = service.transaction {
        val parents = it.select(kclass = TestTableParent::class)
        this.assert(parents == list)
        return@transaction parents
    }

    fun test_select_children(list: List<TestTable>) = service.query {
        this.assert(it.select(kclass = TestTable::class) == list)
        return@query
    }

    fun test_select_oneParent(id: Int, expected: TestTableParent) = service.query {
        val actual = it.select(kclass = TestTableParent::class, pk = id)
        this.assert(expected.id == actual!!.id)
        this.assert(actual == expected)
    }

    fun test_select_oneChildren(id: Int, expected: TestTable) = service.transaction {
        val actual = it.select(kclass = TestTable::class, pk = id)
        this.assert(expected.id == actual!!.id)
        this.assert(actual == expected)
    }

    fun test_select_parentPage(page: Page<TestTableParent>): List<TestTableParent> = service.query {
        it.select(kclass = TestTableParent::class, page = page)
    }

    fun test_join_query() = service.transaction {
        //Query elements with join

        val actual = it.query(output = TestOutput::class) {
            //language=SQLite
            """
                SELECT
                    C.id as child_id,
                    P.id as parent_id,
                    C.col12 as child_col12,
                    P.col13 as parent_col13
                FROM TestTable as C
                    JOIN main.TestTableParent P on P.id = C.id
                    WHERE P.col13 LIKE 'copy %'
                        AND C.col12 LIKE 'child_%'
            """.trimIndent()
        }

        assert(
            actual == listOf(
                TestOutput(child_id = 4, parent_id = 4, child_col12 = "child_0", parent_col13 = "copy 1"),
                TestOutput(child_id = 5, parent_id = 5, child_col12 = "child_1", parent_col13 = "copy 2"),
                TestOutput(child_id = 6, parent_id = 6, child_col12 = "child_2", parent_col13 = "copy 3"),
                TestOutput(child_id = 7, parent_id = 7, child_col12 = "child_3", parent_col13 = "copy 4"),
                TestOutput(child_id = 8, parent_id = 8, child_col12 = "child_4", parent_col13 = "copy 5")
            )
        )
    }

    fun test_output_join_query() = service.query {
        //Query elements with join

        val actual = it.query(output = TestOutput::class) {
            //language=SQLite
            """
                SELECT
                    C.id as ${it.out(TestOutput::child_id)},
                    P.id as ${it.out(TestOutput::parent_id)},
                    C.col12 as ${it.out(TestOutput::child_col12)},
                    P.col13 as ${it.out(TestOutput::parent_col13)}
                FROM TestTable as C
                    JOIN main.TestTableParent P on P.id = C.id
                    WHERE P.col13 LIKE 'copy %'
                        AND C.col12 LIKE 'child_%'
            """.trimIndent()
        }

        assert(
            actual == listOf(
                TestOutput(child_id = 4, parent_id = 4, child_col12 = "child_0", parent_col13 = "copy 1"),
                TestOutput(child_id = 5, parent_id = 5, child_col12 = "child_1", parent_col13 = "copy 2"),
                TestOutput(child_id = 6, parent_id = 6, child_col12 = "child_2", parent_col13 = "copy 3"),
                TestOutput(child_id = 7, parent_id = 7, child_col12 = "child_3", parent_col13 = "copy 4"),
                TestOutput(child_id = 8, parent_id = 8, child_col12 = "child_4", parent_col13 = "copy 5")
            )
        )
    }

    fun test_input_output_join_query() = service.transaction {
        //Query elements with join
        val input = TestInput(parent_search = "copy %", child_search = "child_%")
        val actual = it.query(input = input, output = TestOutput::class) {
            //language=SQLite
            """
                SELECT
                    C.id as ${it.out(TestOutput::child_id)},
                    P.id as ${it.out(TestOutput::parent_id)},
                    C.col12 as ${it.out(TestOutput::child_col12)},
                    P.col13 as ${it.out(TestOutput::parent_col13)}
                FROM TestTable as C
                    JOIN main.TestTableParent P on P.id = C.id
                    WHERE P.col13 LIKE ${it.inp(TestInput::parent_search)}
                        AND C.col12 LIKE ${it.inp(TestInput::child_search)}
            """.trimIndent()
        }

        assert(
            actual == listOf(
                TestOutput(child_id = 4, parent_id = 4, child_col12 = "child_0", parent_col13 = "copy 1"),
                TestOutput(child_id = 5, parent_id = 5, child_col12 = "child_1", parent_col13 = "copy 2"),
                TestOutput(child_id = 6, parent_id = 6, child_col12 = "child_2", parent_col13 = "copy 3"),
                TestOutput(child_id = 7, parent_id = 7, child_col12 = "child_3", parent_col13 = "copy 4"),
                TestOutput(child_id = 8, parent_id = 8, child_col12 = "child_4", parent_col13 = "copy 5")
            )
        )
    }

    /**
     * TRANSACTION
     */
    private fun test_transaction_rollback() {
        try {
            service.transaction {
                val allPre = it.select(kclass = TestTableParent::class)
                assert(22 == allPre.size)
                it.drop(TestTableParent::class)
                val allPost = it.select(kclass = TestTableParent::class)
                assert(0 == allPost.size)

                throw Throwable("Failed transaction")

            }
        } catch (e: Throwable) {
            val count = service.query { it.select(TestTableParent::class) }
            assert(22 == count.size)
        }
    }

    private fun test_transaction_savepoint() {
        try {
            service.transaction {
                //GET PRE ALL
                val allPre = it.select(kclass = TestTableParent::class)
                assert(22 == allPre.size)

                //DELETE ONE
                it.delete(allPre[0])

                val savepoint1 = it.savePoint()

                //GET POST ALL
                val allPost = it.select(kclass = TestTableParent::class)
                assert(21 == allPost.size)

                //DROP ALL
                val count = it.delete(TestTableParent::class)
                assert(count == 21)

                val allPost2 = it.select(kclass = TestTableParent::class)
                assert(0 == allPost2.size)

                it.rollbackTo(savePoint = savepoint1)

                val allPost3 = it.select(kclass = TestTableParent::class)
                assert(21 == allPost3.size)

            }
        } catch (e: Throwable) {
            val count = service.query { it.select(TestTableParent::class) }
            assert(21 == count.size, count.size.toString())
        }
    }
}
