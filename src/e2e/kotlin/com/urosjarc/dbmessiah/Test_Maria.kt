package com.urosjarc.dbmessiah

import com.urosjarc.dbmessiah.domain.table.Table
import com.urosjarc.dbmessiah.exceptions.TesterException
import com.urosjarc.dbmessiah.impl.mariadb.MariaSchema
import com.urosjarc.dbmessiah.impl.mariadb.MariaSerializer
import com.urosjarc.dbmessiah.impl.mariadb.MariaService
import com.urosjarc.dbmessiah.types.AllTS
import com.zaxxer.hikari.HikariConfig
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test




open class Test_Maria {
    open var children = mutableListOf<Child>()
    open var parents = mutableListOf<Parent>()

    companion object {
        private lateinit var service: MariaService

        @JvmStatic
        @BeforeAll
        fun init() {
            val conf = HikariConfig().apply {
                this.jdbcUrl = "jdbc:mariadb://localhost:3306/main"
                this.username = "root"
                this.password = "root"
            }
            val ser = MariaSerializer(
                schemas = listOf(
                    MariaSchema(
                        name = "main", tables = listOf(
                            Table(Parent::pk),
                            Table(Child::pk, listOf(Child::fk to Parent::class)),
                        )
                    )
                ),
                globalSerializers = AllTS.basic,
                globalOutputs = listOf(Output::class),
                globalInputs = listOf(Input::class),
            )
            service = MariaService(conf = conf, ser = ser)
        }
    }

    fun seed() {
        service.query {
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
    }


    @BeforeEach
    fun setup() {
        this.seed()
        service.query {
            it.query { "SET FOREIGN_KEY_CHECKS=0;" }
            it.query {
                """
                        CREATE OR REPLACE PROCEDURE main.TestProcedure(parent_pk INT)
                        BEGIN
                            SELECT * FROM main.Parent WHERE pk = 1;
                            SELECT * FROM main.Parent WHERE pk = 1;
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

    @Test
    fun insert() {
    }


}
