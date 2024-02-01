package com.urosjarc.dbmessiah.impl.sqlite

import com.urosjarc.dbmessiah.DbMessiahSerializer
import com.urosjarc.dbmessiah.domain.queries.Page
import com.urosjarc.dbmessiah.domain.queries.Query
import com.urosjarc.dbmessiah.domain.schema.Schema
import com.urosjarc.dbmessiah.domain.serialization.TypeSerializer
import com.urosjarc.dbmessiah.domain.table.Escaper
import kotlin.reflect.KClass


class MariaMessiahSerializer(
    schemas: List<Schema> = listOf(),
    globalSerializers: List<TypeSerializer<*>> = listOf(),
    globalInputs: List<KClass<*>> = listOf(),
    globalOutputs: List<KClass<*>> = listOf(),
    escaper: Escaper? = null
) : DbMessiahSerializer(
    schemas = schemas,
    globalSerializers = globalSerializers,
    globalInputs = globalInputs,
    globalOutputs = globalOutputs,
    escaper = escaper ?: Escaper(type = Escaper.Type.GRAVE_ACCENT, joinStr = ".")
) {

    override val onGeneratedKeysFail: String = "select LAST_INSERT_ID();"

    override fun <T : Any> dropQuery(kclass: KClass<T>): Query {
        val T = this.mapper.getTableInfo(kclass = kclass)
        return Query(sql = "DROP TABLE IF EXISTS ${T.path};")
    }

    override fun <T : Any> createQuery(kclass: KClass<T>): Query {
        val T = this.mapper.getTableInfo(kclass = kclass)

        val col = mutableListOf<String>()
        val constraints = mutableListOf<String>()

        //Primary key
        val autoIncrement = if (T.primaryKey.autoIncrement) "AUTO_INCREMENT" else ""
        col.add("${T.primaryKey.name} ${T.primaryKey.dbType} PRIMARY KEY ${autoIncrement}")

        //Foreign keys
        T.foreignKeys.forEach {
            val isNull = if (it.notNull) "" else "NOT NULL"
            col.add("${it.name} ${it.dbType} $isNull")
            constraints.add("FOREIGN KEY (${it.name}) REFERENCES ${it.foreignTable.name} (${it.foreignTable.primaryKey.name})")
        }

        //Other columns
        T.otherColumns.forEach {
            val isNull = if (it.notNull) "" else "NOT NULL"
            col.add("${it.name} ${it.dbType} $isNull")
        }

        //Connect all column definitions to one string
        val columns = (col + constraints).joinToString(", ")

        //Return created query
        return Query(sql = "CREATE TABLE IF NOT EXISTS ${T.path} ($columns);")
    }

    override fun <T : Any> selectQuery(kclass: KClass<T>): Query {
        val T = this.mapper.getTableInfo(kclass = kclass)
        return Query(sql = "SELECT * FROM ${T.path}")
    }

    override fun <T : Any> selectQuery(kclass: KClass<T>, page: Page<T>): Query {
        val T = this.mapper.getTableInfo(kclass = kclass)
        return Query(sql = "SELECT * FROM ${T.path} ORDER BY `${page.orderBy.name}` ASC LIMIT ${page.limit} OFFSET ${page.offset}")
    }

    override fun <T : Any, K : Any> selectQuery(kclass: KClass<T>, pk: K): Query {
        val T = this.mapper.getTableInfo(kclass = kclass)
        return Query(sql = "SELECT * FROM ${T.path} WHERE ${T.primaryKey.path}=$pk")
    }


    override fun insertQuery(obj: Any): Query {
        val T = this.mapper.getTableInfo(obj = obj)
        return Query(
            sql = "INSERT INTO ${T.path} (${T.sqlInsertColumns()}) VALUES (${T.sqlInsertQuestions()});",
            *T.queryValues(obj = obj),
        )
    }

    override fun updateQuery(obj: Any): Query {
        val T = this.mapper.getTableInfo(obj = obj)
        return Query(
            sql = "UPDATE ${T.path} SET ${T.sqlUpdateColumns()} WHERE ${T.primaryKey.path} = ?;",
            *T.queryValues(obj = obj),
            T.primaryKey.queryValue(obj = obj)
        )
    }

    override fun deleteQuery(obj: Any): Query {
        val T = this.mapper.getTableInfo(obj = obj)
        return Query(
            sql = "DELETE FROM ${T.path} WHERE ${T.primaryKey.path} = ?;",
            T.primaryKey.queryValue(obj)
        )
    }

    override fun <T : Any> deleteQuery(kclass: KClass<T>): Query {
        val T = this.mapper.getTableInfo(kclass = kclass)
        return Query(sql = "DELETE FROM ${T.path};")
    }


}
