package com.urosjarc.dbjesus.mariadb

import com.urosjarc.dbjesus.SqlMapper
import com.urosjarc.dbjesus.SqlSerializer
import com.urosjarc.dbjesus.exceptions.SqlMappingException
import com.urosjarc.dbjesus.extend.capitalized
import com.urosjarc.dbjesus.extend.properties
import kotlin.reflect.KClass


class MariaSerializer(override val mapper: SqlMapper) : SqlSerializer<Int> {
    override fun <T : Any> createSqlStr(kclass: KClass<T>): String {
        val col = mutableListOf<String>()

        kclass.properties.forEach {
            val isNull = if (it.canBeNull) "" else "NOT NULL"
            if (it.name == "id") {
                col.add("${it.name} INT AUTO_INCREMENT")
                col.add("PRIMARY KEY(${it.name})")
            } else if (it.name.startsWith("id_")) {
                val foreignTable = it.name.split("_").last().capitalized
                col.add("${it.name} INT $isNull")
                col.add("constraint fk_type foreign key(${it.name}) references $foreignTable(id)")
            } else {
                val type = this.mapper.getDbType(it.kclass)
                col.add("${it.name} $type")
            }
        }

        val columns = col.joinToString(", ")
        return "CREATE OR REPLACE TABLE ${kclass.simpleName} ($columns);"
    }

    override fun <T : Any> selectSql(kclass: KClass<T>, where: String): String {
        return "SELECT * FROM ${kclass.simpleName} WHERE $where"
    }

    override fun <T : Any> selectOneQuery(kclass: KClass<T>, id: Int): String {
        return "SELECT * FROM ${kclass.simpleName} WHERE id = $id"
    }

    override fun insertQuery(obj: Any): String {
        val objFields = this.mapper.encode(obj = obj, valueOnNull = "NULL")

        val columns = objFields.map { it.name }
        val encodedValues = objFields.map { it.value }

        return "INSERT INTO ${obj::class.simpleName} ($columns) VALUES ($encodedValues)"
    }

    override fun updateQuery(obj: Any): String {

        var id: String? = null
        val updates = this.mapper.encode(obj = obj, valueOnNull = "NULL").map {
            if (it.name == "id") id = it.value
            "${it.name} = ${it.value}"
        }.joinToString(", ")

        if (id == null) throw SqlMappingException("Primary key is missing: $obj")

        return "UPDATE ${obj::class.simpleName} SET $updates WHERE id=$id"
    }
}
