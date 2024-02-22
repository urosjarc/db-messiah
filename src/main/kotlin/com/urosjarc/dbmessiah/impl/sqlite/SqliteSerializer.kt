package com.urosjarc.dbmessiah.impl.sqlite

import com.urosjarc.dbmessiah.Schema
import com.urosjarc.dbmessiah.Serializer
import com.urosjarc.dbmessiah.domain.querie.Query
import com.urosjarc.dbmessiah.domain.serialization.TypeSerializer
import com.urosjarc.dbmessiah.domain.table.Table
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
    globalOutputs = globalOutputs
) {
    override val selectLastId = "SELECT LAST_INSERT_ROWID();"

    override fun <T : Any> createQuery(kclass: KClass<T>): Query {
        val T = this.mapper.getTableInfo(kclass = kclass)

        val col = mutableListOf<String>()
        val constraints = mutableListOf<String>()

        //Primary key
        val autoIncrement = if (T.primaryKey.autoIncrement) "AUTOINCREMENT" else ""
        col.add("${T.primaryKey.name} ${T.primaryKey.dbType} PRIMARY KEY ${autoIncrement}")

        //Foreign keys
        T.foreignKeys.forEach {
            val isNull = if (it.notNull) "NOT NULL" else ""
            val isDeleteCascade = if (it.cascadeDelete) "ON DELETE CASCADE" else ""
            val isUpdateCascade = if (it.cascadeUpdate) "ON UPDATE CASCADE" else ""
            col.add("${it.name} ${it.dbType} $isNull")
            constraints.add(
                "FOREIGN KEY (${it.name}) REFERENCES ${it.foreignTable.name} (${it.foreignTable.primaryKey.name}) $isUpdateCascade $isDeleteCascade"
            )
        }

        //Other columns
        T.otherColumns.forEach {
            val isNull = if (it.notNull) "" else "NOT NULL"
            col.add("${it.name} ${it.dbType} $isNull")
        }

        //Connect all column definitions to one string
        val columns = (col + constraints).joinToString(", ")

        //Return created query
        return Query(sql = "CREATE TABLE IF NOT EXISTS ${T.name} ($columns);")
    }
}
