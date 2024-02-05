package com.urosjarc.dbmessiah.impl.postgresql

import com.urosjarc.dbmessiah.Serializer
import com.urosjarc.dbmessiah.domain.querie.Query
import com.urosjarc.dbmessiah.domain.serialization.TypeSerializer
import kotlin.reflect.KClass


class PgSerializer(
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

    override fun <T : Any> createQuery(kclass: KClass<T>): Query {
        val T = this.mapper.getTableInfo(kclass = kclass)

        val col = mutableListOf<String>()
        val constraints = mutableListOf<String>()

        //Primary key
        val serial = if (T.primaryKey.autoIncrement) "SERIAL" else ""
        col.add("${T.primaryKey.name} $serial PRIMARY KEY")

        //Foreign keys
        T.foreignKeys.forEach {
            val isNull = if (it.notNull) "NOT NULL" else ""
            val isDeleteCascade = if (it.cascadeDelete) "ON DELETE CASCADE" else ""
            val isUpdateCascade = if (it.cascadeUpdate) "ON UPDATE CASCADE" else ""
            col.add("${it.name} ${it.dbType} $isNull")
            constraints.add(
                "FOREIGN KEY (${it.name}) REFERENCES ${it.foreignTable.path} (${it.foreignTable.primaryKey.name}) $isUpdateCascade $isDeleteCascade"
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
        return Query(sql = "CREATE TABLE IF NOT EXISTS ${T.path} ($columns)")
    }

    override fun insertQuery(obj: Any, batch: Boolean): Query {
        val T = this.mapper.getTableInfo(obj = obj)
        var sql = "INSERT INTO ${T.path} (${T.sqlInsertColumns()}) VALUES (${T.sqlInsertQuestions()})"
        sql += if (!batch) " RETURNING ${T.primaryKey.name}" else ""
        return Query(
            sql = sql,
            *T.queryValues(obj = obj),
        )
    }

    override fun <T : Any> callQuery(obj: T): Query {
        val P = this.mapper.getProcedure(obj = obj)
        return Query(
            sql = "SELECT * FROM ${P.path}(${P.sqlArguments()})",
            *P.queryValues(obj = obj)
        )
    }
}
