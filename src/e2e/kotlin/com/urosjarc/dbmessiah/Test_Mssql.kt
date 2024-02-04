package com.urosjarc.dbmessiah

import com.urosjarc.dbmessiah.domain.table.Table
import com.urosjarc.dbmessiah.exceptions.TesterException
import com.urosjarc.dbmessiah.impl.mssql.MssqlSchema
import com.urosjarc.dbmessiah.impl.mssql.MssqlSerializer
import com.urosjarc.dbmessiah.impl.mssql.MssqlService
import com.urosjarc.dbmessiah.types.AllTS
import com.zaxxer.hikari.HikariConfig
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test


open class Test_Mssql {
    open var children = mutableListOf<Child>()
    open var parents = mutableListOf<Parent>()

    companion object {
        private lateinit var service: MssqlService

        @JvmStatic
        @BeforeAll
        fun init() {
            val conf = HikariConfig().apply {
                this.jdbcUrl = "jdbc:sqlserver://localhost:1433;encrypt=false;"
                this.username = "sa"
                this.password = "Root_root1"
            }
            val ser = MssqlSerializer(
                schemas = listOf(
                    MssqlSchema(
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
            service = MssqlService(conf = conf, ser = ser)
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
        service.query {
            try {
                it.query { "CREATE SCHEMA main;" }
            } catch (e: Throwable){}
        }
        this.seed()
        service.query {
            it.query {
                """
                        CREATE OR ALTER PROCEDURE main.TestProcedure(@parent_pk INT)
                        AS
                        BEGIN
                            SELECT * FROM main.Parent WHERE pk = @parent_pk;
                            SELECT * FROM main.Parent WHERE pk = @parent_pk;
                        END;
                    """.trimIndent()
            }
            it.query {
                """
                        CREATE OR ALTER PROCEDURE main.TestProcedureEmpty
                        AS
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
