package com.urosjarc.dbmessiah.impl.postgresql

import com.urosjarc.dbmessiah.Serializer
import com.urosjarc.dbmessiah.data.Query
import com.urosjarc.dbmessiah.data.TypeSerializer
import kotlin.reflect.KClass


public open class PgSerializer(
    schemas: List<PgSchema> = listOf(),
    globalSerializers: List<TypeSerializer<*>> = listOf(),
    globalInputs: List<KClass<*>> = listOf(),
    globalOutputs: List<KClass<*>> = listOf(),
) : Serializer(
    schemas = schemas,
    globalSerializers = globalSerializers,
    globalInputs = globalInputs,
    globalOutputs = globalOutputs,
) {
    override val selectLastId: String? = null
    override fun escaped(name: String): String = "\"$name\""

    override fun <T : Any> createTable(table: KClass<T>): Query {
        val T = this.mapper.getTableInfo(kclass = table)

        val col = mutableListOf<String>()
        val constraints = mutableListOf<String>()

        //PRIMARY KEY
        val type =
            if (T.primaryKey.autoInc) " SERIAL"
            else if (T.primaryKey.autoUUID) " UUID"
            else " INTEGER"

        val default =
            if (T.primaryKey.autoUUID) " DEFAULT gen_random_uuid()"
            else ""

        col.add("${escaped(T.primaryKey.name)}$type PRIMARY KEY$default")

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

    override fun insertRow(row: Any, batch: Boolean): Query {
        val T = this.mapper.getTableInfo(obj = row)
        val escapedColumns = T.sqlInsertColumns { escaped(it) }
        var sql = "INSERT INTO ${escaped(T)} ($escapedColumns) VALUES (${T.sqlInsertQuestions()})"
        sql += if (!batch) " RETURNING ${escaped(T.primaryKey.name)}" else ""
        return Query(
            sql = sql,
            *T.queryValues(obj = row),
        )
    }

    override fun <T : Any> createProcedure(procedure: KClass<T>, procedureBody: String): Query = TODO("Not implemented")
    override fun <T : Any> callProcedure(procedure: T): Query = TODO("Not implemented")
    override fun <T : Any> dropProcedure(procedure: KClass<T>): Query = TODO("Not implemented")


}
