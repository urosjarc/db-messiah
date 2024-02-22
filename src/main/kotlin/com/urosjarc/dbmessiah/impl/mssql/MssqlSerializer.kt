package com.urosjarc.dbmessiah.impl.mssql

import com.urosjarc.dbmessiah.Serializer
import com.urosjarc.dbmessiah.domain.table.Page
import com.urosjarc.dbmessiah.domain.querie.Query
import com.urosjarc.dbmessiah.domain.serialization.TypeSerializer
import kotlin.reflect.KClass


public open class MssqlSerializer(
    schemas: List<MssqlSchema> = listOf(),
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

    override val selectLastId = "SELECT SCOPE_IDENTITY()"

    override fun <T : Any> createQuery(kclass: KClass<T>): Query {
        val T = this.mapper.getTableInfo(kclass = kclass)

        val col = mutableListOf<String>()
        val constraints = mutableListOf<String>()

        //Primary key
        val autoIncrement = if (T.primaryKey.autoIncrement) "IDENTITY(1,1)" else ""
        col.add("${T.primaryKey.name} ${T.primaryKey.dbType} PRIMARY KEY ${autoIncrement}")

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
        return Query(sql = "CREATE TABLE ${T.path} ($columns)")
    }

    override fun <T : Any> query(kclass: KClass<T>, page: Page<T>): Query {
        val T = this.mapper.getTableInfo(kclass = kclass)
        return Query(sql = "SELECT * FROM ${T.path} ORDER BY ${page.orderBy.name} OFFSET ${page.offset} ROWS FETCH NEXT ${page.limit} ROWS ONLY")
    }
    override fun <T : Any> dropQuery(kclass: KClass<T>, cascade: Boolean): Query {
        val T = this.mapper.getTableInfo(kclass = kclass)
        return Query(sql = "DROP TABLE IF EXISTS ${T.path}")
    }
}
