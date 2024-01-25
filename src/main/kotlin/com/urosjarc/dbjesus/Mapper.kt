package com.urosjarc.dbjesus

import com.urosjarc.dbjesus.domain.*
import com.urosjarc.dbjesus.exceptions.MapperException
import com.urosjarc.dbjesus.extend.ext_javaFields
import com.urosjarc.dbjesus.extend.ext_kclass
import com.urosjarc.dbjesus.extend.ext_properties
import java.sql.ResultSet
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.full.primaryConstructor

class Mapper(
    private val tables: List<Table>,
    private val globalSerializers: List<TypeSerializer<*>>
) {
    init {
        this.test()
    }

    private fun test() {
        for (table in this.tables) {
            for (prop in table.kClass.ext_properties) {
                this.getSerializer(
                    tableKClass = table.kClass,
                    propKClass = prop.kclass
                )
            }
        }
    }

    private fun getTable(obj: Any): Table? = this.getTable(tableKClass = obj::class)

    private fun getTable(tableKClass: KClass<*>): Table? = this.tables.firstOrNull { it.kClass == tableKClass }


    private fun decode(resultSet: ResultSet, columnInt: Int, decodeInfo: DecodeInfo): Any? {
        val jdbcType = resultSet.metaData.getColumnType(columnInt)

        for (tser in this.globalSerializers) {
            if (tser.jdbcType.ordinal == jdbcType && decodeInfo.kparam.ext_kclass == tser.kclass) {
                return tser.decoder(resultSet, columnInt, decodeInfo)
            }
        }

        throw MapperException("Serializer missing for: $decodeInfo")
    }

    /**
     * For every table property check if serializer is registered!!!!
     */
    fun getDbType(tableKClass: KClass<*>, propKClass: KClass<*>): String = this.getSerializer(tableKClass = tableKClass, propKClass = propKClass).dbType

    fun getSerializer(tableKClass: KClass<*>, propKClass: KClass<*>): TypeSerializer<*> {
        val tableSerializers = this.getTable(tableKClass = tableKClass)?.tableSerializers ?: listOf()

        //If serializer is not found in table nor global serializers then something must be wrong!
        return (tableSerializers + this.globalSerializers)
            .firstOrNull { it.kclass == propKClass } ?: throw MapperException("Serializer for type '${propKClass.simpleName}' not found in '${tableKClass.simpleName}' nor in global serializers")
    }

    fun getSerializer(propKClass: KClass<*>): TypeSerializer<*> =
        this.globalSerializers.firstOrNull { it.kclass == propKClass } ?: throw MapperException("Serializer for type '${propKClass.simpleName}' not found in global serializers")

    /**
     * Be carefull not to put primary key to list of obj properties!!!!!!!!
     */
    fun <T : Any> getObjProperties(obj: T): ObjProperties {
        //If table is not found then this obj is used as input obj for query
        val table = this.getTable(obj)

        //What are we searching for?
        val objProps = mutableListOf<ObjProperty<*>>()
        var primaryKeyProp: ObjProperty<*>? = null

        //Searching domain
        for (prop in obj::class.ext_javaFields) {

            //Search for serializer in table and global serializers
            val serializer = this.getSerializer(tableKClass = obj::class, propKClass = prop.ext_kclass) as TypeSerializer<Any>

            //Build obj property wrapper
            val objProp: ObjProperty<Any> = ObjProperty(
                name = prop.name, value = prop.get(obj),
                property = prop, serializer = serializer
            )

            //If table is found and prop matches with primary key then we found the guy.
            //Primary key should be saved separatly from other properties
            if (table?.primaryKey != null && prop.name == table.primaryKey.name) {
                primaryKeyProp = objProp
                continue //BECASE YOU DONT WANT TO HAVE PRIMARY KEY IN OBJPROPS
            } else objProps.add(objProp)

        }

        return ObjProperties(primaryKey = primaryKeyProp, list = objProps)
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
                throw MapperException("Decoding error", cause = e)
            }
        }

        try {
            return constructor.callBy(args = args)
        } catch (e: Throwable) {
            throw MapperException("Class ${kclass.simpleName} can't be constructed with arguments: $args", e)
        }
    }
}
