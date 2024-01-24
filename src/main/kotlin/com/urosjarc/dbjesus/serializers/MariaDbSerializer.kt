package com.urosjarc.dbjesus.serializers

import com.urosjarc.dbjesus.DbMapper
import com.urosjarc.dbjesus.DbSerializer
import com.urosjarc.dbjesus.domain.Encoders
import com.urosjarc.dbjesus.domain.InsertQuery
import com.urosjarc.dbjesus.domain.Page
import com.urosjarc.dbjesus.domain.Query
import com.urosjarc.dbjesus.extend.capitalized
import com.urosjarc.dbjesus.extend.properties
import com.urosjarc.dbjesus.impl.basicDbTypeSerializers
import kotlin.reflect.KClass


class MariaDbSerializer : DbSerializer<Int> {

    override val mapper = DbMapper(dbTypeSerializers = basicDbTypeSerializers)

    override fun createQuery(kclass: KClass<Any>): Query {
        val col = mutableListOf<String>()

        kclass.properties.forEach {
            val isNull = if (it.canBeNull) "" else "NOT NULL"
            if (it.name == "id") {
                col.add("${it.name} INTEGER PRIMARY KEY AUTOINCREMENT")
                col.add("PRIMARY KEY(${it.name})")
            } else if (it.name.startsWith("id_")) {
                val foreignTable = it.name.split("_").last().capitalized
                col.add("${it.name} INT $isNull")
                col.add("CONSTRAINT fk_${kclass.simpleName}_${foreignTable} FOREIGN KEY(${it.name}) REFERENCES $foreignTable(id)")
            } else {
                val type = this.mapper.getDbType(it.kclass)
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

    override fun selectOneQuery(kclass: KClass<Any>, id: Int): Query {
        return Query(sql = "SELECT * FROM ${kclass.simpleName} WHERE id=$id")
    }

    override fun insertQuery(obj: Any): InsertQuery {
        val op = this.mapper.getObjProperties(obj = obj, primaryKey = "id")
        return InsertQuery(
            sql = "INSERT INTO ${obj::class.simpleName} (${op.sqlInsertColumns()}) VALUES (${op.sqlInsertValues()})",
            encoders = op.encoders, values = op.values, jdbcTypes = op.jdbcTypes
        )
    }

    override fun updateQuery(obj: Any): Query {
        val op = this.mapper.getObjProperties(obj = obj, primaryKey = "id")
        return Query(
            sql = "UPDATE ${obj::class.simpleName} SET ${op.sqlUpdate()} WHERE id=${op.primaryKey.value}",
            encoders = op.encoders, values = op.values
        )
    }

    override fun query(getEscapedQuery: (encoders: Encoders) -> Query): Query {
        val encoders = Encoders(this.mapper)
        return getEscapedQuery(encoders)
    }
}
