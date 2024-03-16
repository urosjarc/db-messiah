package com.urosjarc.dbmessiah.impl.sqlite

import com.urosjarc.dbmessiah.Schema
import com.urosjarc.dbmessiah.Serializer
import com.urosjarc.dbmessiah.data.Query
import com.urosjarc.dbmessiah.data.TypeSerializer
import com.urosjarc.dbmessiah.domain.Table
import kotlin.reflect.KClass


public open class SqliteSerializer(
    tables: List<Table<*>> = listOf(),
    globalSerializers: List<TypeSerializer<*>> = listOf(),
    globalInputs: List<KClass<*>> = listOf(),
    globalOutputs: List<KClass<*>> = listOf(),
) : Serializer(
    schemas = listOf(Schema(name = "main", tables = tables)),
    globalSerializers = globalSerializers,
    globalInputs = globalInputs,
    globalOutputs = globalOutputs,
    allowAutoUUID = false
) {
    override var selectLastId: String? = "SELECT LAST_INSERT_ROWID();"
    override fun escaped(name: String): String = "\"$name\""

    override fun <T : Any> createTable(table: KClass<T>): Query {
        val T = this.mapper.getTableInfo(kclass = table)

        val col = mutableListOf<String>()
        val constraints = mutableListOf<String>()

        //Primary key
        val autoIncrement = if (T.primaryColumn.autoInc) " AUTOINCREMENT" else ""
        col.add("${escaped(T.primaryColumn.name)} ${T.primaryColumn.dbType} PRIMARY KEY${autoIncrement}")

        //Foreign keys
        T.foreignColumns.forEach {
            val notNull = if (it.notNull) " NOT NULL" else ""
            val unique = if (it.unique) " UNIQUE" else ""
            val deleteCascade = if (it.cascadeDelete) " ON DELETE CASCADE" else ""
            val updateCascade = if (it.cascadeUpdate) " ON UPDATE CASCADE" else ""
            col.add("${escaped(it.name)} ${it.dbType}$notNull$unique")
            constraints.add(
                "FOREIGN KEY (${escaped(it.name)}) REFERENCES ${escaped(it.foreignTable.name)} (${escaped(it.foreignTable.primaryColumn.name)})$updateCascade$deleteCascade"
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
        return Query(sql = "CREATE TABLE IF NOT EXISTS ${escaped(T.name)} ($columns)")
    }

    override fun <T : Any> createProcedure(procedure: KClass<T>, procedureBody: String): Query {
        TODO("Not implemented")
    }

    override fun <T : Any> callProcedure(procedure: T): Query {
        TODO("Not implemented")
    }

    override fun <T : Any> dropProcedure(procedure: KClass<T>): Query {
        TODO("Not implemented")
    }

}
