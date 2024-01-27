package com.urosjarc.dbmessiah

import com.urosjarc.dbmessiah.domain.columns.C
import com.urosjarc.dbmessiah.domain.columns.ForeignColumn
import com.urosjarc.dbmessiah.domain.columns.OtherColumn
import com.urosjarc.dbmessiah.domain.columns.PrimaryColumn
import com.urosjarc.dbmessiah.domain.schema.Schema
import com.urosjarc.dbmessiah.domain.serialization.DecodeInfo
import com.urosjarc.dbmessiah.domain.serialization.TypeSerializer
import com.urosjarc.dbmessiah.domain.table.Table
import com.urosjarc.dbmessiah.domain.table.TableInfo
import com.urosjarc.dbmessiah.exceptions.MapperException
import com.urosjarc.dbmessiah.exceptions.SerializerException
import com.urosjarc.dbmessiah.extend.ext_canBeNull
import com.urosjarc.dbmessiah.extend.ext_javaFields
import com.urosjarc.dbmessiah.extend.ext_kclass
import org.apache.logging.log4j.kotlin.logger
import java.sql.ResultSet
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.full.primaryConstructor

class Mapper(
    private val escaper: String,
    private val schemas: List<Schema>,
    private val globalSerializers: List<TypeSerializer<*>>
) {
    private val tableInfos = mutableListOf<TableInfo>()

    private val tableKClass_to_SchemaMap = mutableMapOf<KClass<*>, Schema>()
    private val tableKClass_to_tableInfo = mutableMapOf<KClass<*>, TableInfo>()

    val log = this.logger()

    init {
        this.init()
    }

    private fun init() {

        //Register tables
        this.schemas.forEach { schema ->
            schema.tables.forEach { table ->
                val tableInfo = this.register(schema = schema, table = table)
                this.tableKClass_to_SchemaMap[table.kclass] = schema
                this.tableKClass_to_tableInfo[table.kclass] = tableInfo
            }
        }

        //Fill parent foreign table fields after all tables are registered!
        for (tableInfo in this.tableInfos) {
            this.log.info("Registering: ${tableInfo.path}")
            for (column in tableInfo.foreignKeys) {
                this.log.info("\t- ${column.name}: ${column.kclass.simpleName} ${column.dbType}")
                val ftInfo = this.tableInfos.firstOrNull { it.kclass == column.foreignTableKClass }
                column.foreignTable = ftInfo
            }
        }

        //Test registered tables
        this.schemas.forEach {
            Tester(tables = it.tables, tableInfos = this.tableInfos).also {
                it.test_0_if_all_tables_has_unique_paths()
                it.test_1_if_table_registered_multiple_times()
                it.test_2_if_primary_key_in_foreign_keys()
                it.test_3_if_foreign_key_points_to_registered_table()
                it.test_4_if_foreign_key_registered_multiple_times()
                it.test_5_if_constraints_registered_multiple_times()
            }
        }

    }

    private fun register(schema: Schema, table: Table<*>): TableInfo {
        //All pk and fk properties
        val pkFkProperties = mutableSetOf(table.primaryKey)

        //Primary columns
        val pkSerializer = this.getSerializer(schema = schema, table = table, propKClass = table.primaryKey.ext_kclass)
        val pkcons = table.primaryKeyConstraints
        val pkColumn = PrimaryColumn(
            name = table.primaryKey.name,
            notNull = !table.primaryKey.ext_canBeNull, autoIncrement = pkcons.contains(C.AUTO_INC), unique = pkcons.contains(C.UNIQUE),
            kprop = table.primaryKey, kclass = table.primaryKey.ext_kclass, dbType = pkSerializer.dbType, jdbcType = pkSerializer.jdbcType,
            encoder = pkSerializer.encoder, decoder = pkSerializer.decoder,
        )

        //Foreign columns
        val fkColumns = mutableListOf<ForeignColumn>()
        for ((fromProp, toKClass) in table.foreignKeys) {
            val serializer = this.getSerializer(schema = schema, table = table, propKClass = fromProp.ext_kclass)
            val fkcons = table.constraintsFor(kprop = fromProp)
            val column = ForeignColumn(
                name = fromProp.name, notNull = fromProp.ext_canBeNull,
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
            val serializer = this.getSerializer(schema = schema, table = table, propKClass = it.ext_kclass)
            val otcons = table.constraintsFor(kprop = it)
            val column = OtherColumn(
                name = it.name, notNull = it.ext_canBeNull,
                kprop = it, kclass = it.ext_kclass, dbType = serializer.dbType, jdbcType = serializer.jdbcType,
                encoder = serializer.encoder, decoder = serializer.decoder, unique = otcons.contains(C.UNIQUE)
            )
            otherColumns.add(column)
        }

        val tableInfo = TableInfo(
            escaper = escaper,
            schema = schema.name, kclass = table.kclass,
            primaryKey = pkColumn, foreignKeys = fkColumns, otherColumns = otherColumns,
            serializers = table.serializers
        )

        this.tableInfos.add(tableInfo)

        return tableInfo
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
    private fun getSerializer(schema: Schema, table: Table<*>, propKClass: KClass<*>): TypeSerializer<*> {
        return (table.serializers + schema.serializers + this.globalSerializers)
            .firstOrNull { it.kclass == propKClass } ?: throw SerializerException("Serializer for type '${propKClass.simpleName}' not found in schema '${schema.name}' nor in table '${table.name}")
    }

    fun getSerializer(tableKClass: KClass<*>, propKClass: KClass<*>): TypeSerializer<*> {
        val schemaSerializers = this.tableKClass_to_SchemaMap[tableKClass]?.serializers ?: listOf()
        val tableInfoSerializers = this.tableKClass_to_tableInfo[tableKClass]?.serializers ?: listOf()
        return (tableInfoSerializers + schemaSerializers + this.globalSerializers).firstOrNull { it.kclass == propKClass }
            ?: throw SerializerException("Serializer for type '${propKClass.simpleName}' not found in global serializers")
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
