package com.urosjarc.dbjesus.serializers

import com.urosjarc.dbjesus.Mapper
import com.urosjarc.dbjesus.Serializer
import com.urosjarc.dbjesus.domain.*
import com.urosjarc.dbjesus.domain.queries.InsertQuery
import com.urosjarc.dbjesus.domain.queries.Page
import com.urosjarc.dbjesus.domain.queries.Query
import com.urosjarc.dbjesus.domain.queries.QueryBuilder
import com.urosjarc.dbjesus.domain.serialization.TypeSerializer
import com.urosjarc.dbjesus.domain.table.Table
import kotlin.reflect.KClass


class MariaDbSerializer(
    tables: List<Table>,
    globalSerializers: List<TypeSerializer<*>>
) : Serializer {

    override val mapper = Mapper(tables = tables, globalSerializers = globalSerializers)


    override fun <T : Any> createQuery(kclass: KClass<T>): Query {
        val col = mutableListOf<String>()
        val ti = this.mapper.getTableInfo(kclass = kclass)

        ti.columns.forEach {

            //Informations about class property
            val isNull = if (it.canBeNull) "" else "NOT NULL"
            val autoIncrement = if (it.table.autoIncrement) "AUTOINCREMENT" else ""
            val nameAndType = "${it.name} ${it.dbType}"

            //Specific logic for type of class property
            when (it.type) {
                PType.PRIMARY_KEY -> {
                    col.add("$nameAndType PRIMARY KEY ${autoIncrement}")
                    col.add("PRIMARY KEY(${it.name})")
                }

                PType.FOREIGN_KEY -> {
                    col.add("$nameAndType $isNull")
                    col.add("CONSTRAINT fk_${it.name}_${it.foreignTable?.name} FOREIGN KEY(${it.name}) REFERENCES ${it.foreignTable?.name}(${it.foreignTable?.primaryKey})")
                }

                PType.OTHER -> {
                    col.add("$nameAndType ")
                }
            }
        }

        val columns = col.joinToString(", ")
        return Query(sql = "CREATE OR REPLACE TABLE ${ti.table.name} ($columns);")
    }

    override fun <T : Any> selectAllQuery(kclass: KClass<T>): Query {
        val ti = this.mapper.getTableInfo(kclass = kclass)
        return Query(sql = "SELECT * FROM ${ti.table.name}")
    }

    override fun <T : Any> selectPageQuery(kclass: KClass<T>, page: Page<T>): Query {
        val ti = this.mapper.getTableInfo(kclass = kclass)
        return Query(sql = "SELECT * FROM ${ti.table.name} ORDER BY ${page.orderBy.name} ${page.sort} LIMIT ${page.limit} OFFSET ${page.offset}")
    }

    override fun <T : Any, K : Any> selectOneQuery(kclass: KClass<T>, pkValue: K): Query {
        val ti = this.mapper.getTableInfo(kclass = kclass)
        return Query(sql = "SELECT * FROM ${ti.table.name} WHERE ${ti.table.primaryKey.name}=$pkValue")
    }

    override fun insertQuery(obj: Any): InsertQuery {
        val ti = this.mapper.getTableInfo(obj = obj)
        return InsertQuery(
            sql = "INSERT INTO ${ti.table.name} (${ti.table.sqlInsertColumns()}) VALUES (${op.sqlInsertValues()});",
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
