package com.urosjarc.diysqlservice.impl

import com.urosjarc.diysqlservice.SqlGenerator
import com.urosjarc.diysqlservice.domain.SqlMapper
import com.urosjarc.diysqlservice.extend.canBeNull
import com.urosjarc.diysqlservice.extend.kclass
import org.apache.logging.log4j.kotlin.logger
import kotlin.reflect.KClass
import kotlin.reflect.full.declaredMemberProperties


class MariaSqlGenerator(override val sqlMapper: SqlMapper) : SqlGenerator {

    private val log = this.logger()

    override fun <T : Any> createTable(kclass: KClass<T>): String {
        val tableName = kclass.simpleName
        val col = mutableListOf<String>()

        //Todo: Make this prettier
        kclass.declaredMemberProperties.forEach {
            val isNull = if (it.canBeNull) "" else "NOT NULL"
            if (it.name == "id") {
                col.add("${it.name} INT AUTO_INCREMENT")
                col.add("PRIMARY KEY(${it.name})")
            } else if (it.name.startsWith("id")) {
                val foreignTable = it.name.split("_").last().capitalize()
                col.add("${it.name} INT $isNull")
                col.add("constraint fk_type foreign key(${it.name}) references $foreignTable(id)")
            } else {
                val type = this.sqlMapper.getDbType(it.kclass)
                col.add("${it.name} $type")
            }
        }
        val columns = col.joinToString(", ")
        return "CREATE OR REPLACE TABLE $tableName ($columns);"
    }

    override fun <T : Any> selectTable(kclass: KClass<T>): String {
        return "SELECT * FROM ${kclass.simpleName}"
    }

    override fun insertTable(obj: Any): String {
        val sqlMap = this.sqlMapper.encodeObj(obj = obj)
        val columns = sqlMap.keys.joinToString(", ")
        val values = sqlMap.values.joinToString(", ")
        return "INSERT INTO ${obj::class.simpleName} ($columns) VALUES ($values)"
    }

    override fun updateTable(obj: Any): String {
        val sqlMap = this.sqlMapper.encodeObj(obj = obj)
        val id: String? = sqlMap.value("id")
        if (id == null) this.log.error("Primary key is missing: $obj")
        val columns = sqlMap.merge { key, value -> "$key = $value" }.joinToString(", ")
        return "UPDATE ${obj::class.simpleName} SET $columns WHERE id=$id"
    }
}
