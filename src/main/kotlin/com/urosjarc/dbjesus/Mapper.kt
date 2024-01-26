package com.urosjarc.dbjesus

import com.urosjarc.dbjesus.domain.columns.C
import com.urosjarc.dbjesus.domain.columns.ForeignColumn
import com.urosjarc.dbjesus.domain.columns.OtherColumn
import com.urosjarc.dbjesus.domain.columns.PrimaryColumn
import com.urosjarc.dbjesus.domain.serialization.DecodeInfo
import com.urosjarc.dbjesus.domain.serialization.TypeSerializer
import com.urosjarc.dbjesus.domain.table.Table
import com.urosjarc.dbjesus.domain.table.TableInfo
import com.urosjarc.dbjesus.exceptions.MapperException
import com.urosjarc.dbjesus.exceptions.SerializerException
import com.urosjarc.dbjesus.extend.ext_canBeNull
import com.urosjarc.dbjesus.extend.ext_javaFields
import com.urosjarc.dbjesus.extend.ext_kclass
import java.sql.ResultSet
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.full.primaryConstructor

class Mapper(
    private val tables: List<Table<*>>,
    private val globalSerializers: List<TypeSerializer<*>>
) {
    private val tableInfos = mutableListOf<TableInfo>()

    init {
        this.init()
    }

    private fun init() {

        //Register tables
        this.tables.forEach { this.register(table = it) }

        //Fill parent foreign table fields after all tables are registered!
        for (tableInfo in this.tableInfos) {
            for (column in tableInfo.foreignKeys) {
                val ftInfo = this.tableInfos.firstOrNull { it.kclass == column.foreignTableKClass }
                column.foreignTable = ftInfo
            }
        }

        //Test registered tables
        Tester(tables = this.tables, tableInfos = this.tableInfos).also {
            it.test_0_if_all_tables_has_unique_names()
            it.test_1_if_table_registered_multiple_times()
            it.test_2_if_primary_key_in_foreign_keys()
            it.test_3_if_foreign_key_points_to_registered_table()
            it.test_4_if_foreign_key_registered_multiple_times()
            it.test_5_if_constraints_registered_multiple_times()
        }

    }

    private fun register(table: Table<*>) {
        //All pk and fk properties
        val pkFkProperties = mutableSetOf(table.primaryKey)

        //Primary columns
        val pkSerializer = this.getSerializer(tableKClass = table.kclass, propKClass = table.primaryKey.ext_kclass)
        val pkcons = table.primaryKeyConstraints
        val pkColumn = PrimaryColumn(
            name = table.primaryKey.name, value = null,
            notNull = !table.primaryKey.ext_canBeNull, autoIncrement = pkcons.contains(C.AUTO_INC), unique = pkcons.contains(C.UNIQUE),
            kprop = table.primaryKey, kclass = table.primaryKey.ext_kclass, dbType = pkSerializer.dbType, jdbcType = pkSerializer.jdbcType,
            encoder = pkSerializer.encoder, decoder = pkSerializer.decoder,
        )

        //Foreign columns
        val fkColumns = mutableListOf<ForeignColumn>()
        for ((fromProp, toKClass) in table.foreignKeys) {
            val serializer = this.getSerializer(tableKClass = table.kclass, propKClass = fromProp.ext_kclass)
            val fkcons = table.constraintsFor(kprop = fromProp)
            val column = ForeignColumn(
                name = fromProp.name, value = null, notNull = fromProp.ext_canBeNull,
                kprop = fromProp, kclass = fromProp.ext_kclass, dbType = serializer.dbType, jdbcType = serializer.jdbcType,
                foreignTable = null, foreignTableKClass = toKClass,
                encoder = serializer.encoder, decoder = serializer.decoder, unique = fkcons.contains(C.UNIQUE)
            )
            fkColumns.add(column)
            pkFkProperties.add(fromProp)
        }

        //Other columns
        val otherColumns = mutableListOf<OtherColumn>()
        table.kclass.ext_javaFields.filter { !pkFkProperties.contains(it) }.forEach {
            val serializer = this.getSerializer(tableKClass = table.kclass, propKClass = it.ext_kclass)
            val otcons = table.constraintsFor(kprop = it)
            val column = OtherColumn(
                name = it.name, value = null, notNull = it.ext_canBeNull,
                kprop = it, kclass = it.ext_kclass, dbType = serializer.dbType, jdbcType = serializer.jdbcType,
                encoder = serializer.encoder, decoder = serializer.decoder, unique = otcons.contains(C.UNIQUE)
            )
            otherColumns.add(column)
        }

        this.tableInfos.add(
            TableInfo(
                name = table.name, kclass = table.kclass,
                primaryKey = pkColumn, foreignKeys = fkColumns, otherColumns = otherColumns
            )
        )
    }

    private fun decode(resultSet: ResultSet, columnInt: Int, decodeInfo: DecodeInfo): Any? {
        val jdbcType = resultSet.metaData.getColumnType(columnInt)

        for (tser in this.globalSerializers) {
            if (tser.jdbcType.ordinal == jdbcType && decodeInfo.kparam.ext_kclass == tser.kclass) {
                return tser.decoder(resultSet, columnInt, decodeInfo)
            }
        }

        throw SerializerException("Serializer missing for: $decodeInfo")
    }

    fun getTableInfo(obj: Any): TableInfo = this.getTableInfo(obj::class as KClass<*>)
    fun getTableInfo(kclass: KClass<*>) = this.tableInfos.firstOrNull { it.kclass == kclass } ?: throw SerializerException("Table '${kclass.simpleName}' missing in registered tables")
    fun getSerializer(tableKClass: KClass<*>, propKClass: KClass<*>): TypeSerializer<*> {
        val tableSerializers = this.tables.firstOrNull { it.kclass == tableKClass }?.tableSerializers ?: listOf()

        //If serializer is not found in table nor global serializers then something must be wrong!
        return (tableSerializers + this.globalSerializers)
            .firstOrNull { it.kclass == propKClass } ?: throw SerializerException("Serializer for type '${propKClass.simpleName}' not found in '${tableKClass.simpleName}' nor in global serializers")
    }

    fun getSerializer(propKClass: KClass<*>): TypeSerializer<*> =
        this.globalSerializers.firstOrNull { it.kclass == propKClass } ?: throw SerializerException("Serializer for type '${propKClass.simpleName}' not found in global serializers")

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
