package com.urosjarc.dbjesus.domain

import com.urosjarc.dbjesus.extend.kclass
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

    fun getDbType(kclass: KClass<*>): String = this.sqlMappings.first { it.kclass == kclass }.dbType

    fun decode(kp: KParameter, i: Int, rs: ResultSet): Any? {
        val jdbcType = rs.metaData.getColumnType(i)
        val jdbcTypeName = rs.metaData.getColumnTypeName(i)
        val name = rs.metaData.getColumnName(i)
        for (map in this.sqlMappings) {
            if (map.jdbcType.ordinal == jdbcType && kp.kclass == map.kclass) {
                return map.decoding(i, rs)
            }
        }
        throw SqlMappingException("Missing decoding for: ResultSet(column=$name, jdbcTypeName=${jdbcTypeName}) -> ObjField(name=${kp.name}, type=${kp.type})")
    }

    fun <T : Any> decode(kclass: KClass<T>, resultSet: ResultSet): T {
        val constructor = kclass.primaryConstructor!!

        val args = mutableMapOf<KParameter, Any?>()

        for (kp in constructor.parameters) {
            try {
                val columnInt = resultSet.findColumn(kp.name)
                args[kp] = this.decode(kp = kp, i = columnInt, rs = resultSet)
            } catch (e: Throwable) {
                throw SqlMappingException("Decoding error", cause = e)
            }
        }

        try {
            return constructor.callBy(args = args)
        } catch (e: Throwable) {
            throw SqlMappingException("Cant construct '$kclass' with arguments: $args", e)
        }
    }

    fun encode(obj: Any, valueOnNull: String): List<ObjField> = obj::class.declaredMemberProperties.map {
        val prop = (it as KProperty1<Any, *>)
        ObjField(
            name = it.name,
            encodedValue = this.encodeValue(value=prop.get(obj)) ?: valueOnNull,
            kProperty1 = prop
        )
    }

    private fun encodeValue(value: Any?): String? {
        if (value == null) return null
        for (map in this.sqlMappings) {
            if (map.kclass == value::class) {
                return (map as SqlMapping<Any>).encoding(value)
            }
        }
        throw SQLException("Missing DbMapping encoding for: ${value::class}")
    }
}
