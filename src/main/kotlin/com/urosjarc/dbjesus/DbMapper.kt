package com.urosjarc.dbjesus

import com.urosjarc.dbjesus.domain.DbTypeSerializer
import com.urosjarc.dbjesus.domain.DecodeInfo
import com.urosjarc.dbjesus.domain.ObjProperties
import com.urosjarc.dbjesus.domain.ObjProperty
import com.urosjarc.dbjesus.exceptions.DbMapperException
import com.urosjarc.dbjesus.extend.javaFields
import com.urosjarc.dbjesus.extend.kclass
import java.sql.ResultSet
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.KProperty1
import kotlin.reflect.full.primaryConstructor

class DbMapper(val dbTypeSerializers: List<DbTypeSerializer<*>>) {
    fun getDbType(kclass: KClass<Any>): String =
        this.getDbTypeSerializer(kclass = kclass).dbType

    fun getDbTypeSerializer(kclass: KClass<Any>): DbTypeSerializer<Any> {
        val serializer = this.dbTypeSerializers.firstOrNull { it.kclass == kclass }
        if (serializer == null) throw DbMapperException("Serializer missing for class: ${kclass.simpleName}")

        @Suppress("UNCHECKED_CAST")
        return serializer as DbTypeSerializer<Any>
    }

    /**
     * Be carefull not to put primary key to list of obj properties!!!!!!!!
     */
    fun getObjProperties(obj: Any, primaryKey: String): ObjProperties {
        val objProps = mutableListOf<ObjProperty>()
        var primaryKeyProp: ObjProperty? = null
        for (prop in obj::class.javaFields) {

            @Suppress("UNCHECKED_CAST")
            prop as KProperty1<Any, *>

            val serializer = this.dbTypeSerializers.firstOrNull { prop.kclass == it.kclass }
            if (serializer == null) throw DbMapperException("Serializer missing for class: ${prop.kclass.simpleName}")

            @Suppress("UNCHECKED_CAST")
            serializer as DbTypeSerializer<Any>

            val objProp = ObjProperty(name = prop.name, value = prop.get(obj), property = prop, serializer = serializer)

            if (prop.name == primaryKey) {
                primaryKeyProp = objProp
                continue //BECASE YOU DONT WANT TO HAVE PRIMARY KEY IN OBJPROPS
            } else objProps.add(objProp)
        }
        if (primaryKeyProp == null) throw DbMapperException("Primary key '$primaryKey' not found in: $obj")
        return ObjProperties(primaryKey = primaryKeyProp, list = objProps)
    }

    private fun decode(resultSet: ResultSet, columnInt: Int, decodeInfo: DecodeInfo): Any? {
        val jdbcType = resultSet.metaData.getColumnType(columnInt)

        for (tser in this.dbTypeSerializers) {
            if (tser.jdbcType.ordinal == jdbcType && decodeInfo.kparam.kclass == tser.kclass) {
                return tser.decoder(resultSet, columnInt, decodeInfo)
            }
        }

        throw DbMapperException("Serializer missing for: $decodeInfo")
    }

    fun <T : Any> decode(resultSet: ResultSet, kclass: KClass<T>): T {
        val constructor = kclass.primaryConstructor!!
        val args = mutableMapOf<KParameter, Any?>()

        for (kparam in constructor.parameters) {
            try {
                val columnInt = resultSet.findColumn(kparam.name)
                val decodeInfo = DecodeInfo(kclass = kclass, kparam = kparam)
                args[kparam] = this.decode(resultSet = resultSet, columnInt = columnInt, decodeInfo = decodeInfo)
            } catch (e: Throwable) {
                throw DbMapperException("Decoding error", cause = e)
            }
        }

        try {
            return constructor.callBy(args = args)
        } catch (e: Throwable) {
            throw DbMapperException("Class ${kclass.simpleName} can't be constructed with arguments: $args", e)
        }
    }
}
