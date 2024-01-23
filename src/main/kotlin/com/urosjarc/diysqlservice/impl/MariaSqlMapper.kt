package com.urosjarc.diysqlservice.impl

import com.urosjarc.diysqlservice.domain.SqlMapper
import com.urosjarc.diysqlservice.SqlService
import com.urosjarc.diysqlservice.extend.canBeNull
import com.urosjarc.diysqlservice.extend.kclass
import com.zaxxer.hikari.HikariConfig
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.KProperty1
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.primaryConstructor


class MariaSqlMapper(
    config: HikariConfig,
    override val sqlMapper: SqlMapper,
    val test: Boolean = false
) : SqlService(config = config) {

    override fun <T : Any> createTable(kclass: KClass<T>): String {
        val tableName = kclass.simpleName
        val col = mutableListOf<String>()

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
                val type = this.sqlMapper.dbType(it.kclass)
                col.add("${it.name} $type")
            }
        }
        val columns = col.joinToString(", ")
        return "CREATE OR REPLACE TABLE $tableName ($columns);"
    }

    override fun <T : Any> selectTable(kclass: KClass<T>): MutableList<T> {
        return this.exec(sql = "SELECT * FROM ${kclass.simpleName}") {
            this.sqlMapper.decode(kclass = kclass, resultSet = it)
        }
    }

    override fun insertTable(obj: Any) {
        val sqlMap = this.sqlMapper.encodeObj(obj = obj)
        val columns = sqlMap.keys.joinToString(", ")
        val values = sqlMap.values.joinToString(", ")
        this.exec(sql = "INSERT INTO ${obj::class.simpleName} ($columns) VALUES ($values)") {}
    }

    override fun updateTable(obj: Any) {
        var primary: KProperty1<out Any, *>? = null
        val fields = obj::class.declaredMemberProperties.map {
            if (it.name == "id") primary = it
            Pair(it.name, (it as KProperty1<Any, *>).get(receiver = obj))
        }
        if (primary == null) return this.log.error("Primary key is missing: $obj")
        val id = (primary as KProperty1<Any, *>).get(receiver = obj)
        val columns =
            fields.map { Pair(it.first, this.sqlMapper.encodeValue(obj = it.second)) }.map { "${it.first} = ${it.second}" }.joinToString(", ")
        this.exec(sql = "UPDATE ${obj::class.simpleName} SET $columns WHERE id=$id") {}
    }

    override fun <T : Any> selectTable(cls: KClass<T>, sql: String): MutableList<T> {
        val results = mutableListOf<T>()

        this.exec(sql = sql) { rs ->
            val constructor = cls.primaryConstructor!!

            val args = mutableMapOf<KParameter, Any?>()

            for (kp in constructor.parameters) {
                try {
                    val columnInt = rs.findColumn(kp.name)
                    args[kp] = this.sqlMapper.decode(i = columnInt, rs = rs, kp = kp)
                } catch (e: Throwable) {
                    this.log.error(e)
                    continue
                }
            }
            this.log.info(args.map { "${it.key.name} ${it.value}" })
            results.add(constructor.callBy(args = args))
        }

        return results
    }

}
