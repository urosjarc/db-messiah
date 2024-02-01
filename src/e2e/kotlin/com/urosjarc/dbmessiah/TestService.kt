package com.urosjarc.dbmessiah

import com.urosjarc.dbmessiah.exceptions.TesterException

class TestService(val service: DbMessiahService) {

    private fun test(body: () -> Unit) {
        //Reseting tables
        this.service.query {
            it.create(Child::class)
            it.create(Parent::class)
            it.delete(Child::class)
            it.delete(Parent::class)
        }

        val numParents = 5
        val numChildren = 5
        val children = mutableListOf<Child>()
        val parents = mutableListOf<Parent>()

        //Inserting tables
        this.service.query {
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

        body()
    }

    fun `test drop`() = this.test {

        this.service.query {
            val preParents = it.select(kclass = Parent::class)
            try {
                it.drop(kclass = Parent::class)
                it.select(kclass = Parent::class)
                throw TesterException("Table should not be able to exists")
            } catch (e: Throwable) {

            }
        }

    }

}
