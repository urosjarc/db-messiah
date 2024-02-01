package com.urosjarc.dbmessiah

import com.urosjarc.dbmessiah.*
import com.urosjarc.dbmessiah.exceptions.TesterException
import com.urosjarc.dbmessiah.impl.sqlite.SqliteSerializer
import com.urosjarc.dbmessiah.types.AllTS
import com.zaxxer.hikari.HikariConfig
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertContains


class Test_Sqlite {

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
        println("asdfasdfasdf")
        //Reseting tables
        service.query {
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

    @Test
    fun `test drop`() {
        service.query {
            it.select(kclass = Parent::class)
            it.drop(kclass = Parent::class)
            val e = assertThrows<Throwable> {
                it.select(kclass = Parent::class)
            }
            assertContains(charSequence = e.message.toString(), other = "missing database", message = e.toString())
        }
    }
}
