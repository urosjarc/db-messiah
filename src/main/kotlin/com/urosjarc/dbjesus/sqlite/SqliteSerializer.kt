package com.urosjarc.dbjesus.mariadb

import com.urosjarc.dbjesus.SqlMapper
import com.urosjarc.dbjesus.SqlSerializer
import com.urosjarc.dbjesus.domain.InsertQuery
import com.urosjarc.dbjesus.domain.Query
import com.urosjarc.dbjesus.extend.capitalized
import com.urosjarc.dbjesus.extend.properties
import kotlin.reflect.KClass


class SqliteSerializer : SqlSerializer<Int> {

    override val mapper = SqlMapper(sqlTypeSerializers = baseMappings)

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

    override fun selectAllQuery(kclass: KClass<Any>): Query {
        return Query(sql = "SELECT * FROM ${kclass.simpleName}")
    }

    override fun selectOneQuery(kclass: KClass<Any>, id: Int): Query {
        return Query(
            sql = "SELECT * FROM ${kclass.simpleName} WHERE id=?", encoders = listOf(
            )
        )
    }

    override fun insertQuery(obj: Any): InsertQuery {
        val op = this.mapper.getObjProperties(obj = obj, primaryKey = "id")
        return InsertQuery(
            sql = "INSERT INTO ${obj::class.simpleName} (${op.sqlInsertColumns()}) VALUES (${op.sqlInsertValues()})",
            encoders = op.encoders
        )
    }

    override fun updateQuery(obj: Any): Query {
        val op = this.mapper.getObjProperties(obj = obj, primaryKey = "id")
        return Query(
            sql = "UPDATE ${obj::class.simpleName} SET ${op.sqlUpdate()} WHERE id=${op.primaryKey.value}",
            encoders = op.encoders.apply { add(op.primaryKey.encoder) }
        )
    }
}
