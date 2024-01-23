package com.urosjarc.diysqlservice.domain

import com.urosjarc.diysqlservice.extend.kclass
import org.apache.logging.log4j.kotlin.logger
import java.sql.ResultSet
import java.sql.SQLException
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.KProperty1
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.primaryConstructor

class SqlMapper(val sqlMappings: List<SqlMapping<*>>) {

    private val log = this.logger()

    fun dbType(kclass: KClass<*>): String = this.sqlMappings.first { it.kclass == kclass }.dbType

    fun decode(kp: KParameter, i: Int, rs: ResultSet): Any? {
        val javaSql = rs.metaData.getColumnType(i)
        val javaSqlName = rs.metaData.getColumnTypeName(i)
        val name = rs.metaData.getColumnName(i)
        for (map in this.sqlMappings) {
            if (map.jdbcType.ordinal == javaSql && kp.kclass == map.kclass) {
                return map.decoding(i, rs)
            }
        }
        throw SqlMappingException("Missing DbMapping decoding for: $name[$javaSqlName] -> ${kp.name}[${kp.type}]")
    }

    fun <T : Any> decode(kclass: KClass<T>, resultSet: ResultSet): T {
        val constructor = kclass.primaryConstructor!!

        val args = mutableMapOf<KParameter, Any?>()

        for (kp in constructor.parameters) {
            try {
                val columnInt = resultSet.findColumn(kp.name)
                args[kp] = this.decode(kp = kp, i = columnInt, rs = resultSet)
            } catch (e: Throwable) {
                this.log.error(e)
                continue
            }
        }

        try {
            return constructor.callBy(args = args)
        } catch (e: Throwable) {
            throw SqlMappingException("Cant construct '$kclass' with arguments: $args", e)
        }
    }

    fun encodeObj(obj: Any): SqlMap {
        val keys = mutableListOf<String>()
        val values = mutableListOf<String>()
        obj::class.declaredMemberProperties.map {
            keys.add(it.name)
            values.add(this.encodeValue(obj = (it as KProperty1<Any, *>).get(receiver = obj)))
        }
        return SqlMap(keys = keys, values = values)
    }

    fun encodeValue(obj: Any?): String {
        if (obj == null) return "NULL"
        for (map in this.sqlMappings) {
            if (map.kclass == obj::class) {
                return (map as SqlMapping<Any>).encoding(obj)
            }
        }
        throw SQLException("Missing DbMapping encoding for: ${obj::class}")
    }
}
