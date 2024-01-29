package com.urosjarc.dbmessiah.tests

import com.urosjarc.dbmessiah.Service
import com.urosjarc.dbmessiah.domain.queries.Page
import com.urosjarc.dbmessiah.domain.test.TestInput
import com.urosjarc.dbmessiah.domain.test.TestOutput
import com.urosjarc.dbmessiah.domain.test.TestTable
import com.urosjarc.dbmessiah.domain.test.TestTableParent
import com.urosjarc.dbmessiah.exceptions.TesterException
import java.sql.SQLException

class TestService(val service: Service) {

    fun test_drop_parent() {
        this.service.drop(TestTableParent::class)
        try {
            this.service.select(TestTableParent::class)
        } catch (e: SQLException){
            return assert(e.message.toString().contains("no such table"), e.message)
        }
        throw TesterException("Table should not exist!")
    }

    fun test_drop_children() {
        this.service.drop(TestTable::class)
        try {
            this.service.select(TestTable::class)
        } catch (e: SQLException){
            return assert(e.message.toString().contains("no such table"), e.message)
        }
        throw TesterException("Table should not exist!")
    }

    fun test_create_parent() {
        this.assert(this.service.create(kclass = TestTableParent::class))
        this.assert(this.service.select(kclass = TestTableParent::class).isEmpty())
    }

    fun test_insert_children() {
        this.assert(this.service.create(kclass = TestTable::class))
        this.assert(this.service.select(kclass = TestTable::class).isEmpty())
    }

    fun test_insert_parents(): List<TestTableParent> {
        val par0 = TestTableParent()
        val par1 = par0.copy(col13 = "XXX")

        //Create copies of elements on how they should look like in the future
        val par0_copy = par0.copy(id = 1)
        val par1_copy = par1.copy(id = 2)

        this.assert(par0.id == null)
        this.assert(par1.id == null)

        this.assert(par0 != par1)

        this.assert(this.service.insert(obj = par0))
        this.assert(this.service.insert(obj = par1))

        //Test elements and compare it with future copies
        this.assert(par0.id == 1 && par0 == par0_copy)
        this.assert(par1.id == 2 && par1 == par1_copy)

        return listOf(par0, par1)
    }

    fun test_select_parents(list: List<TestTableParent>): List<TestTableParent> {
        val parents = this.service.select(kclass = TestTableParent::class)
        this.assert(parents == list)
        return parents
    }

    fun test_select_oneParent(id: Int, expected: TestTableParent) {
        val actual = this.service.select(kclass = TestTableParent::class, pk = id)
        this.assert(expected.id == actual!!.id)
        this.assert(actual == expected)
    }

    fun test_select_parentPage(page: Page<TestTableParent>): List<TestTableParent> = this.service.select(kclass = TestTableParent::class, page = page)

    fun test_insert_children(list: List<TestTableParent>): List<TestTable> {
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

        this.assert(this.service.insert(obj = child0))
        this.assert(this.service.insert(obj = child1))
        this.assert(this.service.insert(obj = child2))

        //Test elements and compare it with future copies
        this.assert(child0.id == 1 && child0_copy == child0)
        this.assert(child1.id == 2 && child1_copy == child1)
        this.assert(child2.id == 3 && child2_copy == child2)

        return listOf(child0, child1, child2)
    }

    fun test_select_children(list: List<TestTable>) {
        this.assert(this.service.select(kclass = TestTable::class) == list)
    }

    fun test_select_oneChildren(id: Int, expected: TestTable) {
        val actual = this.service.select(kclass = TestTable::class, pk = id)
        this.assert(expected.id == actual!!.id)
        this.assert(actual == expected)
    }

    fun test_join_query() {
        //Query elements with join

        val actual = this.service.query(output = TestOutput::class) {
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

    fun test_output_join_query() {
        //Query elements with join

        val actual = this.service.query(output = TestOutput::class) {
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

    fun test_input_output_join_query() {
        //Query elements with join
        val input = TestInput(parent_search = "copy %", child_search = "child_%")
        val actual = this.service.query(input = input, output = TestOutput::class) {
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

    fun test_update() {
        val preChildren = this.service.select(TestTable::class).toMutableList()

        //Test child
        val preChild = this.service.select(TestTable::class, pk = 1) ?: throw TesterException("Selected child should not be null")
        val preChildSnapshot = preChild.copy()
        assert(preChildren.contains(preChild))
        assert(preChild.col12 == "XXX", msg = preChild.col12)
        preChild.col12 = "xxx"

        val futureChild = preChild.copy(col12 = "xxx")

        //Update child
        this.service.update(obj = preChild)

        val afterUpdateChildren = this.service.select(TestTable::class)

        //Test child
        val postChildren = this.service.select(TestTable::class).toMutableList()
        val postChild = this.service.select(TestTable::class, pk = 1) ?: throw TesterException("Selected child should not be null")
        assert(postChild == preChild)
        assert(postChild == futureChild)
        assert(postChildren.contains(postChild))

        //Test if no other children has changed
        postChildren.remove(postChild)
        preChildren.remove(preChildSnapshot)
        assert(postChildren == preChildren)
    }

    fun test_delete() {
        val preChildren = this.service.select(TestTable::class).toMutableList()
        val preChild = this.service.select(TestTable::class, 1) ?: throw TesterException("Selected child should not be null")
        assert(preChildren.contains(preChild))

        assert(preChild.id != null)
        this.service.delete(preChild)
        assert(preChild.id == null)

        val postChildren = this.service.select(TestTable::class)
        val postChild = this.service.select(TestTable::class, 1)

        assert(postChild == null)
        assert(!postChildren.contains(postChild))

        preChildren.removeIf { it.id == 1 }

        assert(preChildren == postChildren)
    }


    fun test_crud_cycle() {
        //Table drop create
        this.test_drop_children()
        this.test_drop_parent()

        //Create parent entities
        this.test_create_parent()
        this.test_insert_children()
        val parents = this.test_insert_parents()
        this.test_select_parents(parents)

        //Create child entities
        val children = this.test_insert_children(parents)
        this.test_select_children(children)

        //Select elements
        this.test_select_oneParent(parents[0].id!!, parents[0])
        this.test_select_oneChildren(children[0].id!!, children[0])

        // Page elements
        repeat(20) { num -> TestTableParent(col13 = "copy $num").also { assert(this.service.insert(it)) } }
        this.assert(listOf(1, 2, 3, 4, 5) == this.test_select_parentPage(Page(number = 0, orderBy = TestTableParent::id, limit = 5)).map { it.id })
        this.assert(listOf(7, 8, 9, 10, 11, 12) == this.test_select_parentPage(Page(number = 1, orderBy = TestTableParent::id, limit = 6)).map { it.id })
        this.assert(listOf(15, 16, 17, 18, 19, 20, 21) == this.test_select_parentPage(Page(number = 2, orderBy = TestTableParent::id, limit = 7)).map { it.id })
        this.assert(listOf(19, 20, 21, 22) == this.test_select_parentPage(Page(number = 3, orderBy = TestTableParent::id, limit = 6)).map { it.id })

        //Testing join query
        repeat(5) { num -> TestTable(parent_id = num, col12 = "child_$num").also { assert(this.service.insert(it)) } }
        this.test_join_query()
        this.test_output_join_query()
        this.test_input_output_join_query()

        //Update query
        this.test_update()
        this.test_delete()

        //Cleaning
        this.test_drop_children()
        this.test_drop_parent()
    }


    fun assert(value: Boolean, msg: String? = null) {
        if (value != true) throw TesterException(msg ?: "Service test failed!")
    }

}
