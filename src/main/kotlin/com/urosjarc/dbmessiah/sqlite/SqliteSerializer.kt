package com.urosjarc.dbmessiah.sqlite

import com.urosjarc.dbmessiah.Escaper
import com.urosjarc.dbmessiah.Mapper
import com.urosjarc.dbmessiah.Serializer
import com.urosjarc.dbmessiah.domain.queries.Page
import com.urosjarc.dbmessiah.domain.queries.Query
import com.urosjarc.dbmessiah.domain.queries.QueryBuilder
import com.urosjarc.dbmessiah.domain.queries.QueryValue
import com.urosjarc.dbmessiah.domain.schema.Schema
import com.urosjarc.dbmessiah.domain.serialization.TypeSerializer
import com.urosjarc.dbmessiah.exceptions.SerializerException
import kotlin.reflect.KClass


class SqliteSerializer(
    override val schemas: List<Schema>,
    override val globalSerializers: List<TypeSerializer<*>>
) : Serializer {

    override val mapper = Mapper(
        escaper = Escaper(
            type=Escaper.Type.DOUBLE_QUOTES,
            joinStr = "."
        ),
        schemas = schemas.toList(),
        globalSerializers = globalSerializers
    )

    override fun <T : Any> dropQuery(kclass: KClass<T>): Query {
        val T = this.mapper.getTableInfo(kclass = kclass)
        return Query(sql = "DROP TABLE IF EXISTS ${T.path};")
    }

    override fun <T : Any> createQuery(kclass: KClass<T>): Query {
        val T = this.mapper.getTableInfo(kclass = kclass)

        val col = mutableListOf<String>()

        //Primary key
        val autoIncrement = if (T.primaryKey.autoIncrement) "AUTOINCREMENT" else ""
        col.add("${T.primaryKey.name} ${T.primaryKey.dbType} PRIMARY KEY ${autoIncrement}")

        //Foreign keys
        T.foreignKeys.forEach {
            val isNull = if (it.notNull) "" else "NOT NULL"
            val column =
            col.add("${it.name} ${it.dbType} $isNull")
            col.add("FOREIGN KEY (${it.name}) REFERENCES ${it.foreignTable.path} (${it.foreignTable.primaryKey.path})")
        }

        //Other columns
        T.otherColumns.forEach {
            val isNull = if (it.notNull) "" else "NOT NULL"
            col.add("${it.name} ${it.dbType} $isNull")
        }

        //Connect all column definitions to one string
        val columns = col.joinToString(", ")

        //Return created query
        return Query(sql = "CREATE TABLE IF NOT EXISTS ${T.path} ($columns);")
    }

    override fun <T : Any> selectAllQuery(kclass: KClass<T>): Query {
        val T = this.mapper.getTableInfo(kclass = kclass)
        return Query(sql = "SELECT * FROM ${T.path}")
    }

    override fun <T : Any> selectPageQuery(kclass: KClass<T>, page: Page<T>): Query {
        val T = this.mapper.getTableInfo(kclass = kclass)
        return Query(sql = "SELECT * FROM ${T.path} ORDER BY [${page.orderBy.name}] ${page.sort} LIMIT ${page.limit} OFFSET ${page.offset}")
    }

    override fun <T : Any, K : Any> selectOneQuery(kclass: KClass<T>, pkValue: K): Query {
        val T = this.mapper.getTableInfo(kclass = kclass)
        return Query(sql = "SELECT * FROM ${T.path} WHERE ${T.primaryKey.path}=$pkValue")
    }

    override fun insertQuery(obj: Any): Query {
        val T = this.mapper.getTableInfo(obj = obj)
        return Query(
            sql = "INSERT INTO ${T.path} (${T.sqlInsertColumns()}) VALUES (${T.sqlInsertQuestions()});",
            *T.values(obj = obj),
        )
    }

    override fun updateQuery(obj: Any): Query {
        val T = this.mapper.getTableInfo(obj = obj)
        return Query(
            sql = "UPDATE ${T.path} SET ${T.sqlUpdateColumns()} WHERE ${T.primaryKey.path} = ?;",
            *T.values(obj = obj),
            T.primaryKey.queryValue(obj = obj)
        )
    }

    override fun deleteQuery(obj: Any): Query {
        val T = this.mapper.getTableInfo(obj = obj)
        val id = T.primaryKey.getValue(obj = obj) ?: throw SerializerException("Could not retrieve primary key from object: $obj")
        return Query(
            sql = "DELETE FROM ${T.path} WHERE ${T.primaryKey.path} = ?;", )
    }

}
