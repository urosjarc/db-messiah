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
import com.urosjarc.dbmessiah.exceptions.FatalMapperException
import com.urosjarc.dbmessiah.extend.ext_javaFields
import com.urosjarc.dbmessiah.extend.ext_kclass
import com.urosjarc.dbmessiah.tests.TestSchemas
import com.urosjarc.dbmessiah.tests.TestTableInfos
import org.apache.logging.log4j.kotlin.logger
import java.sql.ResultSet
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KParameter
import kotlin.reflect.full.primaryConstructor

class Mapper(
    private val escaper: String,
    private val schemas: List<Schema>,
    private val globalSerializers: List<TypeSerializer<*>>
) {
    private var tableInfos = listOf<TableInfo>()

    private val tableKClass_to_SchemaMap = mutableMapOf<KClass<*>, Schema>()
    private val tableKClass_to_tableInfo = mutableMapOf<KClass<*>, TableInfo>()
    private val fkColumn_to_tableKClass = mutableMapOf<ForeignColumn, KClass<*>>()

    val log = this.logger()

    init {
        this.init()
    }

    private fun init() {

        TestSchemas(schemas = this.schemas, globalSerializers = this.globalSerializers).also {
            //Test emptiness
            it.`1-th Test - If at least one table exist`()
            //Test uniqueness
            it.`2-th Test - If schema registered multiple times`()
            it.`3-th Test - If schemas table registered multiple times`()
            it.`4-th Test - If schemas table foreign key registered multiple times`()
            it.`5-th Test - If schemas table constraints registered multiple times`()
            it.`6-th Test - If schema serializers registered multiple times`()
            it.`7-th Test - If schemas table serializers registered multiple times`()
            //Test connectivity
            it.`8-th Test - If schemas table foreign keys are pointing to registered table with the same type`()
            //Test validity
            it.`9-th Test - If schemas table constraints are valid`()
            it.`10-th Test - If schemas objects have appropriate serializer`()
        }

        /**
         * CREATE TABLE INFOS
         */
        val registeredTableInfos = mutableListOf<TableInfo>()
        this.schemas.forEach { schema ->
            schema.tables.forEach { table ->
                val tableInfo = this.register(schema = schema, table = table)
                this.tableKClass_to_SchemaMap[table.kclass] = schema
                this.tableKClass_to_tableInfo[table.kclass] = tableInfo
                registeredTableInfos.add(tableInfo)
            }
        }
        this.tableInfos = registeredTableInfos.toList()

        /**
         * CONNECT TABLE INFOS OVER FOREIGN KEYS
         */
        for (tableInfo in this.tableInfos) {
            for (column in tableInfo.foreignKeys) {
                val foreignTableKClass = this.fkColumn_to_tableKClass[column] ?: throw FatalMapperException("Could not find link between foreign key and table kclass")
                column.foreignTable = this.tableKClass_to_tableInfo[foreignTableKClass] ?: throw FatalMapperException("Could not find link between foreign table and table info")
            }
        }

        //Test registered tables
        TestTableInfos(tableInfos = this.tableInfos).also {
            //Test emptiness
            it.`1-th Test - If at least one table has been created`()
            //Test consistency
            it.`2-th Test - If table escapers are consistent`()
            //Test uniqueness
            it.`3-th Test - If all tables have unique path, kclass, primaryKey`()
            it.`4-th Test - If all columns are unique`()
            //Test validity
            it.`5-th Test - If all tables own columns`()
            it.`6-th Test - If all foreign columns are valid`()
        }

    }

    private fun register(schema: Schema, table: Table<*>): TableInfo {
        //All pk and fk properties
        val pkFkProperties: MutableSet<Any> = mutableSetOf(table.primaryKey)

        //Primary columns
        val pkSerializer = this.getSerializer(schema = schema, table = table, propKClass = table.primaryKey.ext_kclass)
        val pkcons = table.primaryKeyConstraints
        val pkColumn = PrimaryColumn(
            autoIncrement = pkcons.contains(C.AUTO_INC),
            kprop = table.primaryKey, dbType = pkSerializer.dbType, jdbcType = pkSerializer.jdbcType,
            encoder = pkSerializer.encoder, decoder = pkSerializer.decoder,
        )

        //Foreign columns
        val fkColumns = mutableListOf<ForeignColumn>()
        for ((fromProp, toKClass) in table.foreignKeys) {
            val serializer = this.getSerializer(schema = schema, table = table, propKClass = fromProp.ext_kclass)
            val fkcons = table.constraintsFor(kprop = fromProp)
            val column = ForeignColumn(
                kprop = fromProp, dbType = serializer.dbType, jdbcType = serializer.jdbcType,
                encoder = serializer.encoder, decoder = serializer.decoder, unique = fkcons.contains(C.UNIQUE)
            )
            this.fkColumn_to_tableKClass[column] = toKClass
            fkColumns.add(column)
            pkFkProperties.add(fromProp)
        }

        //Other columns
        val otherColumns = mutableListOf<OtherColumn>()
        table.kclass.ext_javaFields.filter { !pkFkProperties.contains(it) }.forEach {
            val serializer = this.getSerializer(schema = schema, table = table, propKClass = it.ext_kclass)
            val otcons = table.constraintsFor(kprop = it)
            val column = OtherColumn(
                kprop = it, dbType = serializer.dbType, jdbcType = serializer.jdbcType,
                encoder = serializer.encoder, decoder = serializer.decoder, unique = otcons.contains(C.UNIQUE)
            )
            otherColumns.add(column)
        }

        return TableInfo(
            escaper = escaper,
            schema = schema.name, kclass = table.kclass,
            primaryKey = pkColumn, foreignKeys = fkColumns, otherColumns = otherColumns,
            serializers = table.serializers
        )
    }

    private fun decode(resultSet: ResultSet, columnInt: Int, decodeInfo: DecodeInfo): Any? {
        for (tser in this.globalSerializers) {
            if (decodeInfo.kparam.ext_kclass == tser.kclass) {
                return tser.decoder(resultSet, columnInt, decodeInfo)
            }
        }
        throw FatalMapperException("Serializer missing for: $decodeInfo")
    }

    fun getTableInfo(obj: Any): TableInfo = this.getTableInfo(obj::class as KClass<*>)
    fun getTableInfo(kclass: KClass<*>) = this.tableInfos.firstOrNull { it.kclass == kclass } ?: throw FatalMapperException("Table '${kclass.simpleName}' missing in registered tables")
    private fun getSerializer(schema: Schema, table: Table<*>, propKClass: KClass<*>): TypeSerializer<*> {
        val propSerializers = table.columnSerializers.filter { it.first == propKClass }.map { it.second }

        return (propSerializers + schema.serializers + this.globalSerializers)
            .firstOrNull { it.kclass == propKClass } ?: throw FatalMapperException("Serializer for type '${propKClass.simpleName}' not found in schema '${schema.name}' nor in table '${table.name}")
    }

    fun getSerializer(tableKClass: KClass<*>, propKClass: KClass<*>): TypeSerializer<*> {
        val warnings = mutableListOf<String>()
        val schema: Schema? = this.tableKClass_to_SchemaMap[tableKClass]
        if (schema != null) warnings.add("schema '${schema.name}' serializers")
        val tableInfo: TableInfo? = this.tableKClass_to_tableInfo[tableKClass]
        if (tableInfo != null) warnings.add("table '${tableInfo.name}' serializers")

        val tableInfoSerializers = tableInfo?.serializers ?: listOf()
        val schemaSerializers = schema?.serializers ?: listOf()

        return (tableInfoSerializers + schemaSerializers + this.globalSerializers).firstOrNull { it.kclass == propKClass }
            ?: throw FatalMapperException("Serializer for type '${tableKClass.simpleName}.${propKClass.simpleName}' not found in global serializers nor ${warnings.joinToString()}")
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
                throw FatalMapperException("Decoding error", cause = e)
            }
        }

        try {
            return constructor.callBy(args = args)
        } catch (e: Throwable) {
            throw FatalMapperException("Class ${kclass.simpleName} can't be constructed with arguments: $args", e)
        }
    }
}
