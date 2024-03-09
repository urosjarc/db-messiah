package com.urosjarc.dbmessiah

import com.urosjarc.dbmessiah.data.*
import com.urosjarc.dbmessiah.domain.C
import com.urosjarc.dbmessiah.domain.Table
import com.urosjarc.dbmessiah.exceptions.MapperException
import com.urosjarc.dbmessiah.extend.ext_kparams
import com.urosjarc.dbmessiah.extend.ext_kprops
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.KProperty1
import kotlin.reflect.full.primaryConstructor


/**
 * A class that maps Kotlin classes to their associated database tables, procedures, and serializers and vice versa.
 * Mapper is responsible to use kotlin reflection to inspects all user data and create maps from provided information.
 * Kotlin's reflection is CPU expensive so reflection is used only on initialization of building database configuration after that no reflection is used by the sistem.
 *
 * @property schemas A list of all the schemas.
 * @property globalSerializers A list of all the global serializers.
 * @property globalInputs A list of all the global inputs.
 * @property globalOutputs A list of all the global outputs.
 * @property globalProcedures A list of all the global procedures.
 */
public open class MapperCache(
    internal var schemas: List<Schema>,
    internal var globalSerializers: List<TypeSerializer<*>>,
    internal var globalInputs: List<KClass<*>>,
    internal var globalOutputs: List<KClass<*>>,
    internal var globalProcedures: List<KClass<*>>
) {
    /**
     * Map that associates [KClass] with a [TableInfo] object.
     */
    protected val tableKClass_to_tableInfo: MutableMap<KClass<*>, TableInfo> = mutableMapOf()

    /**
     * Represents a mapping between a [KClass] and its corresponding [Procedure].
     */
    protected val procedureKClass_to_procedure: MutableMap<KClass<*>, Procedure> = mutableMapOf()

    /**
     * Represents a mapping between a [KParameter] and its corresponding [TypeSerializer].
     */
    private val kparam_to_serializer: MutableMap<KParameter, TypeSerializer<out Any>> = mutableMapOf()

    /**
     * Represents a mapping between a [KProperty1] and its corresponding [TypeSerializer].
     */
    protected val kprop_to_serializer: MutableMap<KProperty1<out Any, Any?>, TypeSerializer<out Any>> = mutableMapOf()

    /**
     * Represents a list of [TableInfo] where all database table information is located.
     */
    private var tableInfos = listOf<TableInfo>()

    /**
     * List of [Procedure] where all database procedure information is located.
     */
    private var procedures = listOf<Procedure>()

    /**
     * Represents a mapping between a [ForeignColumn] and its corresponding table [KClass].
     */
    private val fkColumn_to_tableKClass = mutableMapOf<ForeignColumn, KClass<*>>()

    /**
     * Represents a mapping between a [KClass] and its corresponding constructor as [KFunction].
     */
    private val kclass_to_constructor = mutableMapOf<KClass<*>, KFunction<Any>?>()

    /**
     * Represents a mapping between a [KClass] and its corresponding list of primary constructor parameters as [KParameter].
     */
    private val kclass_to_constructorParameters = mutableMapOf<KClass<*>, List<KParameter>>()

    /**
     * Represents a mapping between a [KClass] and its corresponding list of properties as [KProperty1].
     */
    private val kclass_to_kprops = mutableMapOf<KClass<*>, List<KProperty1<out Any, *>>>()

    init {
        this.fillReflectionMaps()
        this.fillTablesInfos()
        this.fillProcedures()
    }

    /**
     * Retrieves the [TypeSerializer] for the given [KParameter].
     *
     * @param kparam The [KParameter] for which to retrieve the [TypeSerializer].
     * @return The [TypeSerializer] for the given [KParameter].
     * @throws MapperException if the [TypeSerializer] of the parameter cannot be found.
     */
    protected open fun getSerializer(kparam: KParameter): TypeSerializer<out Any> =
        this.kparam_to_serializer[kparam] ?: throw MapperException("Could not find serializer of parameter: '${kparam}'")

    /**
     * Retrieves the [TypeSerializer] for the given [KProperty1].
     *
     * @param kprop The [KProperty1] for which to retrieve the [TypeSerializer].
     * @return The [TypeSerializer] for the given [KProperty1].
     * @throws MapperException if the [TypeSerializer] of the property cannot be found.
     */
    protected open fun getSerializer(kprop: KProperty1<out Any, Any?>): TypeSerializer<out Any> =
        this.kprop_to_serializer[kprop] ?: throw MapperException("Could not find serializer of property: '${kprop}'")

    /**
     * Retrieves the list of properties [KProperty1] for given [KClass] primary constructor.
     *
     * @param kclass The class for which to retrieve the properties.
     * @return The list of properties for the given class.
     * @throws MapperException if the properties of the class cannot be found.
     */
    private fun getKProps(kclass: KClass<*>): List<KProperty1<out Any, Any?>> =
        this.kclass_to_kprops[kclass] ?: throw MapperException("Could not find properties of class '${kclass.simpleName}'")

    /**
     * Retrieves the primary constructor for a given [KClass].
     *
     * @param kclass The class for which to retrieve the primary constructor.
     * @return The primary constructor for the given class.
     * @throws MapperException if the primary constructor cannot be found.
     */
    protected fun getConstructor(kclass: KClass<*>): KFunction<Any> =
        this.kclass_to_constructor[kclass] ?: throw MapperException("Could not find primary constructor of kclass '${kclass.simpleName}'")

    /**
     * Retrieves the list of constructor parameters [KParameter] for a given class [KClass].
     *
     * @param kclass The class for which to retrieve the constructor parameters.
     * @return The list of constructor parameters for the given class.
     * @throws MapperException if the primary constructor of the class cannot be found.
     */
    protected fun getConstructorParameters(kclass: KClass<*>): List<KParameter> =
        this.kclass_to_constructorParameters[kclass] ?: throw MapperException("Could not find primary constructor of kclass '${kclass.simpleName}'")

    /**
     * Fill association maps for a provided table and its serializers.
     *
     * @param table The table for which to create association maps.
     * @param serializers The list of serializers for the table.
     */
    private fun fillReflectionMaps(table: Table<*>, serializers: List<TypeSerializer<*>>): Unit =
        this.fillReflectionMaps(kclass = table.kclass, serializers = serializers, columnSerializers = table.columnSerializers)

    /**
     * Fills the association maps for a given table class.
     *
     * @param kclass The class of the table for which to fill the association maps.
     * @param columnSerializers The list of column serializers for the table.
     * @param serializers The list of [TypeSerializer] for the table.
     * @param isProcedure Specifies whether the [kclass] is a procedure or not.
     * @throws MapperException if the association maps cannot be filled for the table.
     */
    private fun fillReflectionMaps(
        kclass: KClass<*>,
        columnSerializers: Map<out KProperty1<out Any, Any?>, TypeSerializer<out Any>> = mapOf(),
        serializers: List<TypeSerializer<*>>,
        isProcedure: Boolean = false
    ) {
        val kparams = kclass.ext_kparams
        val kprops = kclass.ext_kprops

        if (kparams == null)
            throw MapperException("Could not get primary constructor parameters for table '${kclass.simpleName}'")
        if (kparams.isEmpty() && !isProcedure)
            throw MapperException("Table '${kclass.simpleName}' have empty primary constructor, which is not allowed!")

        this.kclass_to_constructor[kclass] = kclass.primaryConstructor
        this.kclass_to_constructorParameters[kclass] = kparams
        this.kclass_to_kprops[kclass] = kprops

        kparams.forEach { p ->
            kparam_to_serializer[p] = serializers.firstOrNull { it.kclass == p.type.classifier }
                ?: throw MapperException("Could not find serializer for primary constructor parameter '${kclass.simpleName}'.'${p.name}'")
        }
        kprops.forEach { p ->

            //Priority is on column serializers
            val serializer = columnSerializers[p]

            //If exists first set this guy
            if (serializer != null)
                kprop_to_serializer[p] = serializer
            //If not search for serializer in other table, schema, global serializers.
            else
                kprop_to_serializer[p] = serializers.firstOrNull { it.kclass == p.returnType.classifier }
                    ?: throw MapperException("Could not find serializer for property '${kclass.simpleName}.${p.name}'")
        }
    }

    /**
     * Fill association maps for a provided [schemas], [globalOutputs], [globalProcedures].
     */
    private fun fillReflectionMaps() {
        this.schemas.forEach { s ->
            s.tables.forEach { t ->
                this.fillReflectionMaps(table = t, serializers = (t.serializers + s.serializers + this.globalSerializers))
            }
            s.procedures.forEach {
                this.fillReflectionMaps(kclass = it, serializers = s.serializers + this.globalSerializers, isProcedure = true)
            }
        }
        (this.globalInputs + this.globalOutputs).forEach {
            this.fillReflectionMaps(kclass = it, serializers = this.globalSerializers)
        }
        this.globalProcedures.forEach {
            this.fillReflectionMaps(kclass = it, serializers = this.globalSerializers, isProcedure = true)
        }
    }

    /**
     * Fills the table information from [schemas] tables to [tableInfos].
     * It also links tables to corresponding columns.
     */
    private fun fillTablesInfos() {
        val registeredTableInfos = mutableListOf<TableInfo>()
        this.schemas.forEach { schema ->
            schema.tables.forEach { table ->
                val tableInfo = this.createTableInfo(schema = schema, table = table)
                this.tableKClass_to_tableInfo[table.kclass] = tableInfo
                registeredTableInfos.add(tableInfo)
            }
        }
        this.tableInfos = registeredTableInfos

        /**
         * CONNECT TABLE INFOS OVER FOREIGN KEYS
         */
        for (tableInfo in this.tableInfos) {
            for (column in tableInfo.foreignColumns) {
                val foreignTableKClass =
                    this.fkColumn_to_tableKClass[column] ?: throw MapperException("Could not find link between foreign key and table kclass")
                column.foreignTable = this.tableKClass_to_tableInfo[foreignTableKClass]
                    ?: throw MapperException("Could not find link between foreign table and table info")
            }
        }
    }

    /**
     * Fills the list with procedures from [schemas] to [procedures].
     * Also links schemas to corresponding procedure.
     */
    private fun fillProcedures() {
        val procedures = mutableListOf<Procedure>()
        this.globalProcedures.forEach { kclass ->
            val procedure = this.createProcedure(schema = null, kclass = kclass)
            this.procedureKClass_to_procedure[kclass] = procedure
            procedures.add(procedure)
        }
        this.schemas.forEach { schema ->
            schema.procedures.forEach { kclass ->
                val procedure = this.createProcedure(schema = schema, kclass = kclass)
                this.procedureKClass_to_procedure[kclass] = procedure
                procedures.add(procedure)
            }
        }
        this.procedures = procedures
    }

    /**
     * Creates a [Procedure] object based on the given [schema] and [kclass].
     *
     * @param schema The [Schema] in which this [Procedure] is located.
     * @param kclass The Kotlin [KClass] representing this [Procedure].
     * @return The created [Procedure] object.
     */
    private fun createProcedure(schema: Schema?, kclass: KClass<*>): Procedure {
        val pArgs = mutableListOf<ProcedureArg>()
        val javaFields = this.getKProps(kclass = kclass)
        javaFields.forEach {
            val serializer = this.getSerializer(it)
            val pArg = ProcedureArg(
                kprop = it as KProperty1<Any, Any?>, dbType = serializer.dbType, jdbcType = serializer.jdbcType,
                encoder = serializer.encoder, decoder = serializer.decoder
            )
            pArgs.add(pArg)
        }

        return Procedure(schema = schema?.name, kclass = kclass, args = pArgs)
    }

    /**
     * Creates a [TableInfo] object based on the provided [Schema] and table.
     *
     * @param schema The schema in which the table is located.
     * @param table The table for which to create the TableInfo object.
     * @return The created [TableInfo] object.
     */
    private fun createTableInfo(schema: Schema, table: Table<*>): TableInfo {
        //All pk and fk properties
        val pkFkProperties: MutableSet<Any> = mutableSetOf(table.primaryKey)

        //Primary columns
        val pkSerializer = getSerializer(table.primaryKey)

        val pkColumn = PrimaryColumn(
            kprop = table.primaryKey as KProperty1<Any, Any?>, dbType = pkSerializer.dbType, jdbcType = pkSerializer.jdbcType,
            encoder = pkSerializer.encoder, decoder = pkSerializer.decoder
        )

        //Foreign columns
        val fkColumns = mutableListOf<ForeignColumn>()
        for ((fromProp, toKClass) in table.foreignKeys) {
            val serializer = this.getSerializer(fromProp)
            val fkcons = table.constraints[fromProp] ?: listOf()
            val column = ForeignColumn(
                kprop = fromProp as KProperty1<Any, Any?>,
                dbType = serializer.dbType, jdbcType = serializer.jdbcType,
                encoder = serializer.encoder, decoder = serializer.decoder,
                unique = fkcons.contains(C.UNIQUE),
                cascadeDelete = fkcons.contains(C.CASCADE_DELETE), cascadeUpdate = fkcons.contains(C.CASCADE_UPDATE)
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
            val constraints = table.constraints[it] ?: listOf()
            val column = OtherColumn(
                kprop = it as KProperty1<Any, Any?>, dbType = serializer.dbType, jdbcType = serializer.jdbcType,
                encoder = serializer.encoder, decoder = serializer.decoder, unique = constraints.contains(C.UNIQUE)
            )
            otherColumns.add(column)
        }

        return TableInfo(
            schema = schema.name, kclass = table.kclass,
            primaryColumn = pkColumn, foreignColumns = fkColumns, otherColumns = otherColumns,
            typeSerializers = table.serializers
        )
    }

}
