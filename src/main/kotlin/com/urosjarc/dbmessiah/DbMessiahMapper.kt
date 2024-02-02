package com.urosjarc.dbmessiah

import com.urosjarc.dbmessiah.domain.call.Procedure
import com.urosjarc.dbmessiah.domain.columns.C
import com.urosjarc.dbmessiah.domain.columns.ForeignColumn
import com.urosjarc.dbmessiah.domain.columns.OtherColumn
import com.urosjarc.dbmessiah.domain.columns.PrimaryColumn
import com.urosjarc.dbmessiah.domain.schema.Schema
import com.urosjarc.dbmessiah.domain.serialization.DecodeInfo
import com.urosjarc.dbmessiah.domain.serialization.TypeSerializer
import com.urosjarc.dbmessiah.domain.table.Escaper
import com.urosjarc.dbmessiah.domain.table.Table
import com.urosjarc.dbmessiah.domain.table.TableInfo
import com.urosjarc.dbmessiah.exceptions.MapperException
import com.urosjarc.dbmessiah.exceptions.SerializerException
import com.urosjarc.dbmessiah.tests.TestMapper
import com.urosjarc.dbmessiah.tests.TestUserConfiguration
import org.apache.logging.log4j.kotlin.logger
import java.sql.ResultSet
import kotlin.reflect.*
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.jvm.javaField


class DbMessiahMapper(
    val escaper: Escaper,
    var schemas: List<Schema>,
    var globalSerializers: List<TypeSerializer<*>>,
    var globalInputs: List<KClass<*>>,
    var globalOutputs: List<KClass<*>>,
    var globalProcedures: List<KClass<*>>
) {
    val log = this.logger()

    //All table informations
    var tableInfos = listOf<TableInfo>()

    /**
     * LINKED LISTS
     */

    //Link table to schema
    private val tableKClass_to_SchemaMap = mutableMapOf<KClass<*>, Schema>()

    //Link table to table info
    private val tableKClass_to_tableInfo = mutableMapOf<KClass<*>, TableInfo>()

    //Link foreign column to foreign table
    private val fkColumn_to_tableKClass = mutableMapOf<ForeignColumn, KClass<*>>()

    //Link procedure kclass to procedure
    private val procedureKClass_to_procedure = mutableMapOf<KClass<*>, Procedure>()

    /**
     * MAPPERS
     */

    //Link table kclass to primary constructor
    private val kclass_to_constructor = mutableMapOf<KClass<*>, KFunction<Any>?>()

    //Link table kclass to list of constructor parameters
    private val kclass_to_constructorParameters = mutableMapOf<KClass<*>, List<KParameter>>()

    //Link table kclass to internal fields
    private val kclass_to_kprops = mutableMapOf<KClass<*>, List<KProperty1<out Any, *>>>()

    //Link table constructor parameter to serializer
    private val kparam_to_serializer = mutableMapOf<KParameter, TypeSerializer<out Any>>()

    //Link table property to serializer
    private val kprop_to_serializer = mutableMapOf<KProperty1<out Any, Any?>, TypeSerializer<out Any>>()

    /**
     * GETTERS
     */
    fun getKProps(kclass: KClass<*>): List<KProperty1<out Any, Any?>> =
        this.kclass_to_kprops[kclass] ?: throw MapperException("Could not find properties of class '${kclass.simpleName}'")

    private fun getSerializer(kparam: KParameter): TypeSerializer<out Any> =
        this.kparam_to_serializer[kparam] ?: throw MapperException("Could not find serializer of parameter '${kparam.name}'")

    fun getSerializer(kprop: KProperty1<out Any, Any?>): TypeSerializer<out Any> =
        this.kprop_to_serializer[kprop] ?: throw MapperException("Could not find serializer of property '${kprop.name}'")

    fun getConstructor(kclass: KClass<*>): KFunction<Any> =
        this.kclass_to_constructor[kclass] ?: throw MapperException("Could not find primary constructor of kclass '${kclass.simpleName}'")

    private fun getConstructorParameters(kclass: KClass<*>): List<KParameter> =
        this.kclass_to_constructorParameters[kclass] ?: throw MapperException("Could not find primary constructor of kclass '${kclass.simpleName}'")

    init {
        this.createAssociationMaps()
        this.testSerializer()
        this.createTablesInfos()
        this.testMapper()
    }

    private fun createAssociationMaps(table: Table<*>, serializers: List<TypeSerializer<*>>) {
        this.createAssociationMaps(kclass = table.kclass, serializers = serializers, columnSerializers = table.columnSerializers)
    }

    private fun <T : Any> createAssociationMaps(
        kclass: KClass<*>,
        columnSerializers: List<Pair<KProperty1<out T, *>, TypeSerializer<Any>>> = listOf(),
        serializers: List<TypeSerializer<*>>,
        isProcedure: Boolean = false
    ) {
        val kparams = kclass.primaryConstructor?.parameters?.filter { it.kind == KParameter.Kind.VALUE } // { INSTANCE, EXTENSION_RECEIVER, VALUE }
        val kprops = kclass.memberProperties.filter { it.javaField != null }

        if (kparams == null)
            throw SerializerException("Could not get primary constructor parameters for table '${kclass.simpleName}'")
        if (kparams.isEmpty() && !isProcedure)
            throw SerializerException("Table '${kclass.simpleName}' have empty primary constructor, which is not allowed!")

        this.kclass_to_constructor[kclass] = kclass.primaryConstructor
        this.kclass_to_constructorParameters[kclass] = kparams
        this.kclass_to_kprops[kclass] = kprops

        kparams.forEach { p ->
            kparam_to_serializer[p] = serializers.firstOrNull { it.kclass == p.type.classifier }
                ?: throw SerializerException("Could not find serializer for primary constructor parameter '${kclass.simpleName}'.'${p.name}'")
        }
        kprops.forEach { p ->

            //Priority is on column serializers
            val serializer = columnSerializers.firstOrNull { it.first == p }?.second

            //If exists first set this guy
            if (serializer != null)
                kprop_to_serializer[p] = serializer
            //If not search for serializer in other table, schema, global serializers.
            else
                kprop_to_serializer[p] = serializers.firstOrNull { it.kclass == p.returnType.classifier }
                    ?: throw SerializerException("Could not find serializer for property '${kclass.simpleName}.${p.name}'")
        }
    }

    private fun createAssociationMaps() {
        this.schemas.forEach { s ->
            s.tables.forEach { t ->
                this.createAssociationMaps(table = t, serializers = (t.serializers + s.serializers + this.globalSerializers))
            }
        }
        (this.globalInputs + this.globalOutputs).forEach {
            this.createAssociationMaps<Any>(kclass = it, serializers = this.globalSerializers)
        }
        this.globalProcedures.forEach {
            this.createAssociationMaps<Any>(kclass = it, serializers = this.globalSerializers, isProcedure = true)
        }
    }

    private fun testSerializer() {
        TestUserConfiguration(mapper = this).also {
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
            it.`8-th Test - If schemas table foreign keys are pointing to wrong table`()
            //Test validity
            it.`9-th Test - If schemas table constraints are valid`()
            it.`10-th Test - If all primary keys that are autoincrement have integer dbType`()
            //Test serializability
            it.`11-th Test - If all input classes have imutable and not null properties`()
            it.`12-th Test - If all output classes have no default values`()
        }

    }

    private fun createTablesInfos() {
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
                val foreignTableKClass =
                    this.fkColumn_to_tableKClass[column] ?: throw MapperException("Could not find link between foreign key and table kclass")
                column.foreignTable = this.tableKClass_to_tableInfo[foreignTableKClass]
                    ?: throw MapperException("Could not find link between foreign table and table info")
            }
        }
    }

    fun testMapper() {
        //Test registered tables
        TestMapper(mapper = this).also {
            //Test emptiness
            it.`1-th Test - If at least one table has been created`()
            //Test consistency
            it.`2-th Test - If table escapers are consistent`()
            //Test uniqueness
            it.`3-th Test - If all tables have unique path, kclass, primaryKey`()
            it.`4-th Test - If all columns are unique`()
            //Test validity
            it.`5-th Test - If all tables own their own columns`()
            it.`6-th Test - If all foreign columns are connected to registered table`()
            it.`7-th Test - If all columns have been inited and connected with parent table`()
            it.`8-th Test - If all primary keys that have auto inc are of type integer`()
        }
    }

    private fun register(schema: Schema, table: Table<*>): TableInfo {
        //All pk and fk properties
        val pkFkProperties: MutableSet<Any> = mutableSetOf(table.primaryKey)

        //Primary columns
        val pkSerializer = getSerializer(table.primaryKey)
        val pkcons = table.primaryKeyConstraints
        val pkColumn = PrimaryColumn(
            autoIncrement = pkcons.contains(C.AUTO_INC),
            kprop = table.primaryKey as KMutableProperty1<Any, Any?>, dbType = pkSerializer.dbType, jdbcType = pkSerializer.jdbcType,
            encoder = pkSerializer.encoder, decoder = pkSerializer.decoder
        )

        //Foreign columns
        val fkColumns = mutableListOf<ForeignColumn>()
        for ((fromProp, toKClass) in table.foreignKeys) {
            val serializer = this.getSerializer(fromProp)
            val fkcons = table.constraintsFor(kprop = fromProp)
            val column = ForeignColumn(
                kprop = fromProp as KProperty1<Any, Any?>, dbType = serializer.dbType, jdbcType = serializer.jdbcType,
                encoder = serializer.encoder, decoder = serializer.decoder, unique = fkcons.contains(C.UNIQUE)
            )
            this.fkColumn_to_tableKClass[column] = toKClass
            fkColumns.add(column)
            pkFkProperties.add(fromProp)
        }

        //Other columns
        val otherColumns = mutableListOf<OtherColumn>()
        val javaFields = this.getKProps(kclass = table.kclass)
        javaFields.filter { !pkFkProperties.contains(it) }.forEach {
            val serializer = this.getSerializer(it)
            val constraints = table.constraintsFor(kprop = it)
            val column = OtherColumn(
                kprop = it as KProperty1<Any, Any?>, dbType = serializer.dbType, jdbcType = serializer.jdbcType,
                encoder = serializer.encoder, decoder = serializer.decoder, unique = constraints.contains(C.UNIQUE)
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

    fun <T : Any> getProcedure(obj: T): Procedure = this.getProcedure(kclass = obj::class)
    fun getProcedure(kclass: KClass<*>): Procedure =
        this.procedureKClass_to_procedure[kclass] ?: throw SerializerException("Could not find procedure for kclass: '${kclass.simpleName}'")

    fun <T : Any> getTableInfo(kclass: KClass<T>): TableInfo =
        this.tableKClass_to_tableInfo[kclass] ?: throw SerializerException("Could not find table info for table: '${kclass.simpleName}'")

    fun <T : Any> getTableInfo(obj: T): TableInfo = this.getTableInfo(kclass = obj::class)
    fun decodeMany(resultSet: ResultSet, i: Int, vararg outputs: KClass<*>): List<Any> {
        val output = outputs.getOrNull(i)
        if (output != null) {
            val objs = mutableListOf<Any>()
            while (resultSet.next()) objs.add(this.decode(resultSet, output))
            return objs
        } else return listOf(resultSet.getInt(1))
    }

    fun <T : Any> decode(resultSet: ResultSet, kclass: KClass<T>): T {

        val constructor = this.getConstructor(kclass = kclass)
        val constructorParameters = this.getConstructorParameters(kclass = kclass)

        val args = mutableMapOf<KParameter, Any?>()
        for (kparam in constructorParameters) {
            try {
                val i = resultSet.findColumn(kparam.name)
                val decodeInfo = DecodeInfo(kclass = kclass, kparam = kparam)
                val decoder = this.getSerializer(kparam).decoder
                args[kparam] = decoder(resultSet, i, decodeInfo)
            } catch (e: Throwable) {
                throw MapperException("Decoding error", cause = e)
            }
        }

        try {
            return constructor.callBy(args = args) as T
        } catch (e: Throwable) {
            throw MapperException("Class ${kclass.simpleName} can't be constructed with arguments: $args", e)
        }
    }

}
