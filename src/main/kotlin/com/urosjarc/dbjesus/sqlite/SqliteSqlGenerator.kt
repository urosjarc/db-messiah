package com.urosjarc.dbjesus.mariadb

import com.urosjarc.dbjesus.SqlGenerator
import com.urosjarc.dbjesus.domain.SqlMapper
import com.urosjarc.dbjesus.domain.SqlMappingException
import com.urosjarc.dbjesus.extend.capitalized
import com.urosjarc.dbjesus.extend.fields
import kotlin.reflect.KClass


class SqliteSqlGenerator(override val sqlMapper: SqlMapper) : SqlGenerator<Int> {
    override fun <T : Any> createTable(kclass: KClass<T>): String {
        val col = mutableListOf<String>()

        kclass.fields.forEach {
            val isNull = if (it.canBeNull) "" else "NOT NULL"
            if (it.name == "id") {
                col.add("${it.name} INTEGER PRIMARY KEY AUTOINCREMENT")
                col.add("PRIMARY KEY(${it.name})")
            } else if (it.name.startsWith("id_")) {
                val foreignTable = it.name.split("_").last().capitalized
                col.add("${it.name} INT $isNull")
                col.add("CONSTRAINT fk_${kclass.simpleName}_${foreignTable} FOREIGN KEY(${it.name}) REFERENCES $foreignTable(id)")
            } else {
                val type = this.sqlMapper.getDbType(it.kclass)
                col.add("${it.name} $type")
            }
        }

        val columns = col.joinToString(", ")
        return "CREATE OR REPLACE TABLE ${kclass.simpleName} ($columns);"
    }

    override fun <T : Any> select(kclass: KClass<T>, where: String): String {
        return "SELECT * FROM ${kclass.simpleName} WHERE $where"
    }

    override fun <T : Any> selectOne(kclass: KClass<T>, id: Int): String {
        return "SELECT * FROM ${kclass.simpleName} WHERE id = $id"
    }

    override fun insert(obj: Any): String {
        val objFields = this.sqlMapper.encode(obj=obj, valueOnNull = "NULL")

        val columns = objFields.map { it.name }
        val encodedValues = objFields.map { it.encodedValue }

        return "INSERT INTO ${obj::class.simpleName} ($columns) VALUES ($encodedValues)"
    }

    override fun update(obj: Any): String {

        var id: String? = null
        val updates = this.sqlMapper.encode(obj=obj, valueOnNull = "NULL").map {
            if(it.name == "id") id = it.encodedValue
            "${it.name} = ${it.encodedValue}"
        }.joinToString(", ")

        if(id == null) throw SqlMappingException("Primary key is missing: $obj")

        return "UPDATE ${obj::class.simpleName} SET $updates WHERE id=$id"
    }
}
