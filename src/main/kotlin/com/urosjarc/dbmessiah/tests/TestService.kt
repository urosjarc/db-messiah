package com.urosjarc.dbmessiah.tests

import com.urosjarc.dbmessiah.Service
import com.urosjarc.dbmessiah.domain.queries.Page
import com.urosjarc.dbmessiah.exceptions.TesterException

class TestService(val service: Service) {

    fun test_drop_parent() {
        this.service.drop(TestTableParent::class)
    }

    fun test_drop_children() {
        this.service.drop(TestTable::class)
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

    fun test_select_parentPage(page: Page<TestTableParent>): List<TestTableParent> {
        return this.service.select(kclass = TestTableParent::class, page = page)
    }

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

    fun test_crud_cycle() {
        this.test_drop_children()
        this.test_drop_parent()

        this.test_create_parent()
        this.test_insert_children()
        val parents = this.test_insert_parents()
        this.test_select_parents(parents)
        val children = this.test_insert_children(parents)
        this.test_select_children(children)

        this.test_select_oneParent(parents[0].id!!, parents[0])
        this.test_select_oneChildren(children[0].id!!, children[0])

        repeat(20) {
            val par = TestTableParent(col13 = "copy $it")
            this.service.insert(par)
        }

        this.assert(listOf(1, 2, 3, 4, 5) == this.test_select_parentPage(Page(number = 0, orderBy = TestTableParent::id, limit = 5)).map { it.id })
        this.assert(listOf(6, 7, 8, 9, 10) == this.test_select_parentPage(Page(number = 1, orderBy = TestTableParent::id, limit = 5)).map { it.id })
        this.assert(listOf(11, 12, 13, 14, 15) == this.test_select_parentPage(Page(number = 2, orderBy = TestTableParent::id, limit = 5)).map { it.id })
    }


    fun assert(value: Boolean, msg: String? = null) {
        if (value != true) throw TesterException(msg ?: "Service test failed!")
    }

}
