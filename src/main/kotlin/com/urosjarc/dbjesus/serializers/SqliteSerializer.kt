package com.urosjarc.dbjesus.serializers

import com.urosjarc.dbjesus.Mapper
import com.urosjarc.dbjesus.Serializer
import com.urosjarc.dbjesus.domain.queries.InsertQuery
import com.urosjarc.dbjesus.domain.queries.Page
import com.urosjarc.dbjesus.domain.queries.Query
import com.urosjarc.dbjesus.domain.queries.QueryBuilder
import com.urosjarc.dbjesus.domain.serialization.TypeSerializer
import com.urosjarc.dbjesus.domain.table.Table
import com.urosjarc.dbjesus.exceptions.SerializerException
import kotlin.reflect.KClass


class SqliteSerializer(
    tables: List<Table<*>>,
    globalSerializers: List<TypeSerializer<*>>
) : Serializer {

    override val mapper = Mapper(tables = tables, globalSerializers = globalSerializers)
    override fun <T : Any> dropQuery(kclass: KClass<T>): Query {
        val T = this.mapper.getTableInfo(kclass = kclass)
        return Query(sql = "DROP TABLE ${T.name};")
    }

    override fun <T : Any> createQuery(kclass: KClass<T>): Query {
        val T = this.mapper.getTableInfo(kclass = kclass)

        val col = mutableListOf<String>()

        //Primary key
        if(T.primaryKey != null) {
            val autoIncrement = if (T.primaryKey.autoIncrement) "AUTOINCREMENT" else ""
            col.add("${T.primaryKey.name} ${T.primaryKey.dbType}  PRIMARY KEY ${autoIncrement}")
            col.add("PRIMARY KEY (${T.primaryKey.name})")
        }

        //Foreign keys
        T.foreignKeys.forEach {
            val isNull = if (it.notNull) "" else "NOT NULL"
            col.add("${it.name} ${it.dbType} $isNull")
            col.add("FOREIGN KEY (${it.name}) REFERENCES ${it.foreignTable?.name} (${it.foreignTable?.primaryKey?.name}) ")
        }

        //Other columns
        T.otherColumns.forEach {
            val isNull = if (it.notNull) "" else "NOT NULL"
            col.add("${it.name} ${it.dbType} $isNull")
        }

        //Connect all column definitions to one string
        val columns = col.joinToString(", ")

        //Return created query
        return Query(sql = "CREATE TABLE IF NOT EXISTS ${T.name} ($columns);")
    }

    override fun <T : Any> selectAllQuery(kclass: KClass<T>): Query {
        val T = this.mapper.getTableInfo(kclass = kclass)
        return Query(sql = "SELECT * FROM ${T.name}")
    }

    override fun <T : Any> selectPageQuery(kclass: KClass<T>, page: Page<T>): Query {
        val T = this.mapper.getTableInfo(kclass = kclass)
        return Query(sql = "SELECT * FROM ${T.name} ORDER BY ${page.orderBy.name} ${page.sort} LIMIT ${page.limit} OFFSET ${page.offset}")
    }

    override fun <T : Any, K : Any> selectOneQuery(kclass: KClass<T>, pkValue: K): Query {
        val T = this.mapper.getTableInfo(kclass = kclass)
        if(T.primaryKey == null) throw SerializerException("Table '${T.name}' does not have registered primary key, but you want to search for specific row by it!")
        return Query(sql = "SELECT * FROM ${T.name} WHERE ${T.primaryKey.name}=$pkValue")
    }

    override fun insertQuery(obj: Any): InsertQuery {
        val T = this.mapper.getTableInfo(obj = obj)
        return InsertQuery(
            sql = "INSERT INTO ${T.name} (${T.sqlInsertColumns()}) VALUES (${T.sqlInsertQuestions()});",
            encoders = T.encoders, values = T.values, jdbcTypes = T.jdbcTypes
        )
    }

    override fun updateQuery(obj: Any): Query {
        val T = this.mapper.getTableInfo(obj = obj)
        return Query(
            sql = "UPDATE ${T.name} SET ${T.sqlUpdateColumns()} WHERE id=${T.primaryKey?.value}",
            encoders = T.encoders, values = T.values, jdbcTypes = T.jdbcTypes
        )
    }

    override fun <T : Any> query(sourceObj: T, getSql: (queryBuilder: QueryBuilder) -> String): Query {
        val queryBuilder = QueryBuilder(sourceObj = sourceObj, mapper = this.mapper)
        val sql = getSql(queryBuilder)
        return queryBuilder.build(sql = sql)
    }
}
