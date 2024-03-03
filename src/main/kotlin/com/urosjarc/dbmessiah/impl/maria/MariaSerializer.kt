package com.urosjarc.dbmessiah.impl.maria

import com.urosjarc.dbmessiah.Serializer
import com.urosjarc.dbmessiah.data.Query
import com.urosjarc.dbmessiah.data.TypeSerializer
import kotlin.reflect.KClass


public open class MariaSerializer(
    schemas: List<MariaSchema> = listOf(),
    globalSerializers: List<TypeSerializer<*>> = listOf(),
    globalInputs: List<KClass<*>> = listOf(),
    globalOutputs: List<KClass<*>> = listOf(),
    globalProcedures: List<KClass<*>> = listOf()
) : Serializer(
    schemas = schemas,
    globalSerializers = globalSerializers,
    globalInputs = globalInputs,
    globalOutputs = globalOutputs,
    globalProcedures = globalProcedures
) {

    override val selectLastId: String = "SELECT LAST_INSERT_ID()"
    override fun escaped(name: String): String = "`$name`"

    override fun <T : Any> createTable(table: KClass<T>): Query {
        val T = this.mapper.getTableInfo(kclass = table)

        val col = mutableListOf<String>()
        val constraints = mutableListOf<String>()

        //Primary key
        val autoIncrement = if (T.primaryKey.autoInc) " AUTO_INCREMENT" else ""
        col.add("${escaped(T.primaryKey.name)} ${T.primaryKey.dbType} PRIMARY KEY${autoIncrement}")

        //Foreign keys
        T.foreignKeys.forEach {
            val notNull = if (it.notNull) " NOT NULL" else ""
            val unique = if (it.unique) " UNIQUE" else ""
            val deleteCascade = if (it.cascadeDelete) " ON DELETE CASCADE" else ""
            val updateCascade = if (it.cascadeUpdate) " ON UPDATE CASCADE" else ""
            col.add("${escaped(it.name)} ${it.dbType}$notNull$unique")
            constraints.add(
                "FOREIGN KEY (${escaped(it.name)}) REFERENCES ${escaped(it.foreignTable)} (${escaped(it.foreignTable.primaryKey.name)})$updateCascade$deleteCascade"
            )
        }

        //Other columns
        T.otherColumns.forEach {
            val notNull = if (it.notNull) " NOT NULL" else ""
            val unique = if (it.unique) " UNIQUE" else ""
            col.add("${escaped(it.name)} ${it.dbType}$notNull$unique")
        }

        //Connect all column definitions to one string
        val columns = (col + constraints).joinToString(", ")

        //Return created query
        return Query(sql = "CREATE TABLE IF NOT EXISTS ${escaped(T)} ($columns)")
    }

    public override fun <T : Any> createProcedure(procedure: KClass<T>, procedureBody: String): Query {
        val P = this.mapper.getProcedure(kclass = procedure)
        val args = P.args.map { "${escaped(it.name)} ${it.dbType}" }.joinToString(", ")
        return Query(
            sql = """
                CREATE OR REPLACE PROCEDURE ${escaped(P)}($args)
                BEGIN
                    $procedureBody
                END;
            """.trimIndent()
        )
    }

    public override fun <T : Any> callProcedure(procedure: T): Query {
        val P = this.mapper.getProcedure(obj = procedure)

        return Query(
            sql = "CALL ${escaped(P)}(${P.sqlArguments()})",
            *P.queryValues(obj = procedure)
        )
    }

    override fun <T : Any> dropProcedure(procedure: KClass<T>): Query {
        val P = this.mapper.getProcedure(kclass = procedure)
        return Query(sql = "DROP PROCEDURE IF EXISTS ${escaped(P)}")
    }
}
