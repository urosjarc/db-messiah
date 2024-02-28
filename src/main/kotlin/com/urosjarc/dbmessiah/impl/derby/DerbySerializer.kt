package com.urosjarc.dbmessiah.impl.derby

import com.urosjarc.dbmessiah.Schema
import com.urosjarc.dbmessiah.Serializer
import com.urosjarc.dbmessiah.data.Query
import com.urosjarc.dbmessiah.data.TypeSerializer
import com.urosjarc.dbmessiah.domain.Page
import com.urosjarc.dbmessiah.domain.Table
import kotlin.reflect.KClass


public open class DerbySerializer(
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
    override val selectLastId: String = "SELECT LAST_INSERT_ROWID();"

    override fun <T : Any> createTable(table: KClass<T>): Query {
        val T = this.mapper.getTableInfo(kclass = table)

        val col = mutableListOf<String>()
        val constraints = mutableListOf<String>()

        //Primary key
        val autoIncrement = if (T.primaryKey.autoInc) " GENERATED ALWAYS AS IDENTITY(Start with 1, Increment by 1)" else ""
        col.add("${T.primaryKey.name} ${T.primaryKey.dbType} PRIMARY KEY${autoIncrement}")

        //Foreign keys
        T.foreignKeys.forEach {
            val notNull = if (it.notNull) " NOT NULL" else ""
            val unique = if (it.unique) " UNIQUE" else ""
            val deleteCascade = if (it.cascadeDelete) " ON DELETE CASCADE" else ""
            val updateCascade = if (it.cascadeUpdate) " ON UPDATE CASCADE" else ""
            col.add("${it.name} ${it.dbType}$notNull$unique")
            constraints.add(
                "FOREIGN KEY (${it.name}) REFERENCES ${it.foreignTable.path} (${it.foreignTable.primaryKey.name})$updateCascade$deleteCascade"
            )
        }

        //Other columns
        T.otherColumns.forEach {
            val notNull = if (it.notNull) " NOT NULL" else ""
            val unique = if (it.unique) " UNIQUE" else ""
            col.add("${it.name} ${it.dbType}$notNull$unique")
        }

        //Connect all column definitions to one string
        val columns = (col + constraints).joinToString(", ")

        //Return created query
        return Query(sql = "CREATE TABLE ${T.path} ($columns)")
    }

    override fun <T : Any> selectTable(table: KClass<T>, page: Page<T>): Query {
        val T = this.mapper.getTableInfo(kclass = table)
        return Query(sql = "SELECT * FROM ${T.path} ORDER BY ${page.orderBy.name} OFFSET ${page.offset} ROWS FETCH FIRST ${page.limit} ROWS ONLY")
    }

    override fun <T : Any> createProcedure(procedure: KClass<T>, sql: String): Query {
        TODO("Not implemented")
    }

    override fun <T : Any> dropTable(table: KClass<T>, cascade: Boolean): Query {
        val T = this.mapper.getTableInfo(kclass = table)
        return Query(sql = "DROP TABLE ${T.path}")
    }


    override fun <T : Any> callProcedure(procedure: T): Query {
        TODO("Not implemented")
    }

    override fun <T : Any> dropProcedure(procedure: KClass<T>): Query {
        TODO("Not implemented")
    }
}
