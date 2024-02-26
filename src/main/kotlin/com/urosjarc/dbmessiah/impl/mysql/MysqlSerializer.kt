package com.urosjarc.dbmessiah.impl.mysql

import com.urosjarc.dbmessiah.Serializer
import com.urosjarc.dbmessiah.data.Query
import com.urosjarc.dbmessiah.data.TypeSerializer
import kotlin.reflect.KClass


public open class MysqlSerializer(
    schemas: List<MysqlSchema> = listOf(),
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

    override val selectLastId = "SELECT LAST_INSERT_ID()"

    override fun <T : Any> createTable(table: KClass<T>): Query {
        val T = this.mapper.getTableInfo(kclass = table)

        val col = mutableListOf<String>()
        val constraints = mutableListOf<String>()

        //Primary key
        val autoIncrement = if (T.primaryKey.autoIncrement) "AUTO_INCREMENT" else ""
        col.add("${T.primaryKey.name} ${T.primaryKey.dbType} PRIMARY KEY ${autoIncrement}")

        //Foreign keys
        T.foreignKeys.forEach {
            val isNull = if (it.notNull) "NOT NULL" else ""
            val isUnique = if (it.unique) "UNIQUE" else ""
            val isDeleteCascade = if (it.cascadeDelete) "ON DELETE CASCADE" else ""
            val isUpdateCascade = if (it.cascadeUpdate) "ON UPDATE CASCADE" else ""
            col.add("${it.name} ${it.dbType} $isNull $isUnique")
            constraints.add(
                "FOREIGN KEY (${it.name}) REFERENCES ${it.foreignTable.path} (${it.foreignTable.primaryKey.name}) $isUpdateCascade $isDeleteCascade"
            )
        }

        //Other columns
        T.otherColumns.forEach {
            val isNull = if (it.notNull) "" else "NOT NULL"
            val isUnique = if (it.unique) "UNIQUE" else ""
            col.add("${it.name} ${it.dbType} $isNull $isUnique")
        }

        //Connect all column definitions to one string
        val columns = (col + constraints).joinToString(", ")

        //Return created query
        return Query(sql = "CREATE TABLE IF NOT EXISTS ${T.path} ($columns)")
    }

    override fun <T : Any> createProcedure(procedure: KClass<T>, body: () -> String): Query {
        TODO("Not yet implemented")
    }

    override fun <T : Any> callProcedure(procedure: T): Query {
        TODO("Not yet implemented")
    }
}
