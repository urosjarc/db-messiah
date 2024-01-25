package com.urosjarc.dbjesus.serializers

import com.urosjarc.dbjesus.Mapper
import com.urosjarc.dbjesus.Serializer
import com.urosjarc.dbjesus.domain.*
import com.urosjarc.dbjesus.extend.capitalized
import com.urosjarc.dbjesus.extend.ext_properties
import kotlin.reflect.KClass


class MariaDbSerializer(
    tables: List<Table>,
    globalSerializers: List<TypeSerializer<*>>
) : Serializer {

    override val mapper = Mapper(tables = tables, globalSerializers = globalSerializers)


    override fun <T : Any> createQuery(kclass: KClass<T>): Query {
        val col = mutableListOf<String>()

        kclass.ext_properties.forEach {
            val isNull = if (it.canBeNull) "" else "NOT NULL"
            if (it.name == "id") {
                col.add("${it.name} INTEGER PRIMARY KEY AUTOINCREMENT")
                col.add("PRIMARY KEY(${it.name})")
            } else if (it.name.startsWith("id_")) {
                val foreignTable = it.name.split("_").last().capitalized
                col.add("${it.name} INT $isNull")
                col.add("CONSTRAINT fk_${kclass.simpleName}_${foreignTable} FOREIGN KEY(${it.name}) REFERENCES $foreignTable(id)")
            } else {
                val type = this.mapper.getDbType(tableKClass = kclass, propKClass = it.kclass)
                col.add("${it.name} $type")
            }
        }

        val columns = col.joinToString(", ")
        return Query(sql = "CREATE OR REPLACE TABLE ${kclass.simpleName} ($columns);")
    }

    override fun <T : Any> selectAllQuery(kclass: KClass<T>): Query {
        return Query(sql = "SELECT * FROM ${kclass.simpleName}")
    }

    override fun <T : Any> selectPageQuery(kclass: KClass<T>, page: Page<T>): Query {
        return Query(sql = "SELECT * FROM ${kclass.simpleName} ORDER BY ${page.orderBy.name} ${page.sort} LIMIT ${page.limit} OFFSET ${page.offset}")
    }

    override fun <T : Any, K: Any> selectOneQuery(kclass: KClass<T>, id: K): Query {
        return Query(sql = "SELECT * FROM ${kclass.simpleName} WHERE id=$id")
    }

    override fun insertQuery(obj: Any): InsertQuery {
        val op = this.mapper.getObjProperties(obj = obj)
        return InsertQuery(
            sql = "INSERT INTO ${obj::class.simpleName} (${op.sqlInsertColumns()}) VALUES (${op.sqlInsertValues()});",
            encoders = op.encoders, values = op.values, jdbcTypes = op.jdbcTypes
        )
    }

    override fun updateQuery(obj: Any): Query {
        val op = this.mapper.getObjProperties(obj = obj)
        return Query(
            sql = "UPDATE ${obj::class.simpleName} SET ${op.sqlUpdate()} WHERE id=${op.primaryKey?.value}",
            encoders = op.encoders, values = op.values, jdbcTypes = op.jdbcTypes
        )
    }

    override fun <T : Any> query(sourceObj: T, getSql: (queryBuilder: QueryBuilder) -> String): Query {
        val queryBuilder = QueryBuilder(sourceObj = sourceObj, mapper = this.mapper)
        val sql = getSql(queryBuilder)
        return queryBuilder.build(sql = sql)
    }
}
