package com.urosjarc.dbmessiah.impl

import com.urosjarc.dbmessiah.Mapper
import com.urosjarc.dbmessiah.Serializer
import com.urosjarc.dbmessiah.domain.queries.InsertQuery
import com.urosjarc.dbmessiah.domain.queries.Page
import com.urosjarc.dbmessiah.domain.queries.Query
import com.urosjarc.dbmessiah.domain.queries.QueryBuilder
import com.urosjarc.dbmessiah.domain.schema.Schema
import com.urosjarc.dbmessiah.domain.serialization.TypeSerializer
import kotlin.reflect.KClass


class SqliteSerializer(
    override val escaper: String,
    override val schemas: List<Schema>,
    override val globalSerializers: List<TypeSerializer<*>>
) : Serializer {

    override val mapper = Mapper(escaper = escaper, schemas = schemas.toList(), globalSerializers = globalSerializers)

    override fun <T : Any> dropQuery(kclass: KClass<T>): Query {
        val T = this.mapper.getTableInfo(kclass = kclass)
        return Query(sql = "DROP TABLE ${T.path};")
    }

    override fun <T : Any> createQuery(kclass: KClass<T>): Query {
        val T = this.mapper.getTableInfo(kclass = kclass)

        val col = mutableListOf<String>()

        //Primary key
        val autoIncrement = if (T.primaryKey.autoIncrement) "AUTOINCREMENT" else ""
        col.add("'${T.primaryKey.name}' ${T.primaryKey.dbType} PRIMARY KEY ${autoIncrement}")

        //Foreign keys
        T.foreignKeys.forEach {
            val isNull = if (it.notNull) "" else "NOT NULL"
            col.add("'${it.name}' ${it.dbType} $isNull")
            col.add("FOREIGN KEY ('${it.name}') REFERENCES ${it.foreignTable?.path} ('${it.foreignTable?.primaryKey?.name}')")
        }

        //Other columns
        T.otherColumns.forEach {
            val isNull = if (it.notNull) "" else "NOT NULL"
            col.add("'${it.name}' ${it.dbType} $isNull")
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
        return Query(sql = "SELECT * FROM ${T.path} ORDER BY ${page.orderBy.name} ${page.sort} LIMIT ${page.limit} OFFSET ${page.offset}")
    }

    override fun <T : Any, K : Any> selectOneQuery(kclass: KClass<T>, pkValue: K): Query {
        val T = this.mapper.getTableInfo(kclass = kclass)
        return Query(sql = "SELECT * FROM ${T.path} WHERE ${T.primaryKey.name}=$pkValue")
    }

    override fun insertQuery(obj: Any): InsertQuery {
        val T = this.mapper.getTableInfo(obj = obj)
        return InsertQuery(
            sql = "INSERT INTO ${T.path} (${T.sqlInsertColumns()}) VALUES (${T.sqlInsertQuestions()});",
            values = T.values(obj = obj),
        )
    }

    override fun updateQuery(obj: Any): Query {
        val T = this.mapper.getTableInfo(obj = obj)
        return Query(
            sql = "UPDATE ${T.path} SET ${T.sqlUpdateColumns()} WHERE id=${T.primaryKey.value(obj = obj)}",
            values = T.values(obj = obj)
        )
    }

    override fun <T : Any> query(sourceObj: T, getSql: (queryBuilder: QueryBuilder<T>) -> String): Query {
        val queryBuilder = QueryBuilder(sourceObj = sourceObj, mapper = this.mapper)
        val sql = getSql(queryBuilder)
        return queryBuilder.build(sql = sql)
    }
}