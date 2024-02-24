package com.urosjarc.dbmessiah

import com.urosjarc.dbmessiah.data.*
import com.urosjarc.dbmessiah.domain.C
import com.urosjarc.dbmessiah.domain.Table
import com.urosjarc.dbmessiah.exceptions.MapperException
import com.urosjarc.dbmessiah.exceptions.SerializerException
import com.urosjarc.dbmessiah.tests.MapperTests
import com.urosjarc.dbmessiah.tests.UserConfigurationTests
import java.sql.ResultSet
import kotlin.reflect.*
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.jvm.javaField


/**
 * A class that maps Kotlin classes to their associated database tables, procedures, and serializers and vice versa.
 * Mapper is responsible to use kotlin reflection to inspects all user data and create maps from provided information.
 * Kotlin reflection is CPU expensive so reflection is used only when building database configuration after that no reflection is used by the sistem.
 *
 * @property schemas A list of all the schemas.
 * @property globalSerializers A list of all the global serializers.
 * @property globalInputs A list of all the global inputs.
 * @property globalOutputs A list of all the global outputs.
 * @property globalProcedures A list of all the global procedures.
 * @property tableInfos A map of table classes linked to their corresponding table information.
 * @property procedures A map of procedure classes linked to their corresponding procedures.
 * @property tableKClass_to_tableInfo A map of table classes linked to their corresponding table information.
 * @property fkColumn_to_tableKClass A map of foreign key columns linked to their corresponding table classes.
 * @property procedureKClass_to_procedure A map of procedure classes linked to their corresponding procedures.
 * @property kclass_to_constructor A map of classes linked to their constructors.
 * @property kclass_to_constructorParameters A map of classes linked to their constructor parameters.
 * @property kclass_to_kprops A map of classes linked to their KProperty1 objects.
 * @property kparam_to_serializer A map of constructor parameters linked to their serializers.
 * @property kprop_to_serializer A map of KProperty1 objects linked to their serializers.
 */
public class Mapper(
    internal var schemas: List<Schema>,
    private var globalSerializers: List<TypeSerializer<*>>,
    internal var globalInputs: List<KClass<*>>,
    internal var globalOutputs: List<KClass<*>>,
    internal var globalProcedures: List<KClass<*>>
) {

    //All table informations
    internal var tableInfos = listOf<TableInfo>()
    internal var procedures = listOf<Procedure>()

    /**
     * LINKED LISTS
     */

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
     * Checks if the given class is registered or associated by the [Mapper] with [fillAssociationMaps].
     *
     * @param kclass The class to check for.
     * @return True if the class is associated, False otherwise.
     */
    public fun isRegistered(kclass: KClass<*>): Boolean {
        val hasConst = this.kclass_to_constructor.containsKey(key = kclass)
        val hasConstParams = this.kclass_to_constructorParameters.containsKey(key = kclass)
        val hasProps = this.kclass_to_kprops.containsKey(key = kclass)
        return hasProps && hasConst && hasConstParams
    }

    /**
     * Retrieves the list of properties (KProperty1) for a given class (KClass).
     *
     * @param kclass The class for which to retrieve the properties.
     * @return The list of properties for the given class.
     * @throws MapperException if the properties of the class cannot be found.
     */
    internal fun getKProps(kclass: KClass<*>): List<KProperty1<out Any, Any?>> =
        this.kclass_to_kprops[kclass] ?: throw MapperException("Could not find properties of class '${kclass.simpleName}'")

    /**
     * Retrieves the [TypeSerializer] for the given [KParameter].
     *
     * @param kparam The [KParameter] for which to retrieve the [TypeSerializer].
     * @return The [TypeSerializer] for the given [KParameter].
     * @throws MapperException if the [TypeSerializer] of the parameter cannot be found.
     */
    private fun getSerializer(kparam: KParameter): TypeSerializer<out Any> =
        this.kparam_to_serializer[kparam] ?: throw MapperException("Could not find serializer of parameter: '${kparam}'")

    /**
     * Retrieves the [TypeSerializer] for the given [KProperty1].
     *
     * @param kprop The [KProperty1] for which to retrieve the [TypeSerializer].
     * @return The [TypeSerializer] for the given [KProperty1].
     * @throws MapperException if the [TypeSerializer] of the property cannot be found.
     */
    internal fun getSerializer(kprop: KProperty1<out Any, Any?>): TypeSerializer<out Any> {
        return this.kprop_to_serializer[kprop] ?: throw MapperException("Could not find serializer of property: '${kprop}'")
    }

    /**
     * Retrieves the primary constructor for a given class.
     *
     * @param kclass The class for which to retrieve the primary constructor.
     * @return The primary constructor for the given class.
     * @throws MapperException if the primary constructor cannot be found.
     */
    internal fun getConstructor(kclass: KClass<*>): KFunction<Any> =
        this.kclass_to_constructor[kclass] ?: throw MapperException("Could not find primary constructor of kclass '${kclass.simpleName}'")

    /**
     * Retrieves the list of constructor parameters (KParameter) for a given class (KClass).
     *
     * @param kclass The class for which to retrieve the constructor parameters.
     * @return The list of constructor parameters for the given class.
     * @throws MapperException if the primary constructor of the class cannot be found.
     */
    private fun getConstructorParameters(kclass: KClass<*>): List<KParameter> =
        this.kclass_to_constructorParameters[kclass] ?: throw MapperException("Could not find primary constructor of kclass '${kclass.simpleName}'")

    init {
        this.fillAssociationMaps()
        this.testConfiguration()
        this.fillTablesInfos()
        this.fillProcedures()
        this.testMapper()
    }

    /**
     * Fill association maps for a provided table and its serializers.
     *
     * @param table The table for which to create association maps.
     * @param serializers The list of serializers for the table.
     */
    private fun fillAssociationMaps(table: Table<*>, serializers: List<TypeSerializer<*>>): Unit =
        this.fillAssociationMaps(kclass = table.kclass, serializers = serializers, columnSerializers = table.columnSerializers)

    /**
     * Fills the association maps for a given table class.
     * To check if class was associated or registered use [isRegistered] method.
     *
     * @param kclass The class of the table for which to fill the association maps.
     * @param columnSerializers The list of column serializers for the table.
     * @param serializers The list of [TypeSerializer] for the table.
     * @param isProcedure Specifies whether the [kclass] is a procedure or not.
     * @throws SerializerException if the association maps cannot be filled for the table.
     */
    private fun <T : Any> fillAssociationMaps(
        kclass: KClass<*>,
        columnSerializers: List<Pair<KProperty1<out T, *>, TypeSerializer<*>>> = listOf(),
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

    /**
     * Fill association maps for a provided [schemas], [globalOutputs], [globalProcedures].
     *
     * @param table The table for which to create association maps.
     * @param serializers The list of [TypeSerializer] for the table.
     */
    private fun fillAssociationMaps() {
        this.schemas.forEach { s ->
            s.tables.forEach { t ->
                this.fillAssociationMaps(table = t, serializers = (t.serializers + s.serializers + this.globalSerializers))
            }
            s.procedures.forEach {
                this.fillAssociationMaps<Any>(kclass = it, serializers = s.serializers + this.globalSerializers, isProcedure = true)
            }
        }
        (this.globalInputs + this.globalOutputs).forEach {
            this.fillAssociationMaps<Any>(kclass = it, serializers = this.globalSerializers)
        }
        this.globalProcedures.forEach {
            this.fillAssociationMaps<Any>(kclass = it, serializers = this.globalSerializers, isProcedure = true)
        }
    }

    /**
     * This method is used to test the configuration. It performs various tests on the
     * configuration to ensure database type safety. The tests include checking for emptiness, uniqueness, connectivity,
     * validity, and serializability.
     *
     * @see [UserConfigurationTests]
     *
     * @throws Exception if an error occurs during the configuration tests.
     */
    private fun testConfiguration() {
        UserConfigurationTests(mapper = this).also {
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
            //Test global object by uniqueness
            it.`13-th Test - If global procedure registered multiple times`()
            it.`14-th Test - If global outputs registered multiple times`()
            it.`15-th Test - If global inputs registered multiple times`()
            it.`16-th Test - If global serializers registered multiple times`()
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
            for (column in tableInfo.foreignKeys) {
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
     * The testMapper method is used to test the final state of the Mapper.
     * It performs various tests on the configuration to ensure database type safety.
     * The tests include checking for emptiness, uniqueness, connectivity, validity, and serializability.
     *
     * @see UserConfigurationTests
     *
     * @throws Exception if an error occurs during the configuration tests.
     */
    internal fun testMapper() {
        //Test registered tables
        MapperTests(mapper = this).also {
            //Test emptiness
            it.`1-th Test - If at least one table has been created`()
            //Test uniqueness
            it.`3-th Test - If all tables have unique path, kclass, primaryKey`()
            it.`4-th Test - If all columns are unique`()
            //Test validity
            it.`5-th Test - If all tables own their columns`()
            it.`6-th Test - If all foreign columns are connected to registered table`()
            it.`7-th Test - If all columns have been inited and connected with parent table`()
            it.`8-th Test - If all primary keys that have auto inc are of type integer`()
            it.`9-th Test - If all procedures arguments have been inited and connected with its owner`()
            it.`10-th Test - If all procedures own their arguments`()
        }
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
            val constraints = table.constraintsFor(kprop = it)
            val column = OtherColumn(
                kprop = it as KProperty1<Any, Any?>, dbType = serializer.dbType, jdbcType = serializer.jdbcType,
                encoder = serializer.encoder, decoder = serializer.decoder, unique = constraints.contains(C.UNIQUE)
            )
            otherColumns.add(column)
        }

        return TableInfo(
            schema = schema.name, kclass = table.kclass,
            primaryKey = pkColumn, foreignKeys = fkColumns, otherColumns = otherColumns,
            serializers = table.serializers
        )
    }

    /**
     * Retrieves the [Procedure] object based on the provided [obj].
     *
     * @param obj The object for which to retrieve the [Procedure].
     * @return The [Procedure] object.
     * @throws SerializerException If the [Procedure] for the object cannot be found.
     */
    internal fun <T : Any> getProcedure(obj: T): Procedure = this.getProcedure(kclass = obj::class)

    /**
     * Retrieves the [Procedure] object based on the provided [kclass].
     *
     * @param kclass The Kotlin [KClass] representing the [Procedure].
     * @return The [Procedure] object.
     * @throws SerializerException If the [Procedure] for the class cannot be found.
     */
    internal fun getProcedure(kclass: KClass<*>): Procedure =
        this.procedureKClass_to_procedure[kclass] ?: throw SerializerException("Could not find procedure for kclass: '${kclass.simpleName}'")

    /**
     * Retrieves the [TableInfo] object for the given table class.
     *
     * @param kclass The Kotlin class representing the table.
     * @return The [TableInfo] object for the given table class.
     * @throws SerializerException if the table info for the table cannot be found.
     */
    internal fun <T : Any> getTableInfo(kclass: KClass<T>): TableInfo =
        this.tableKClass_to_tableInfo[kclass] ?: throw SerializerException("Could not find table info for table: '${kclass.simpleName}'")

    /**
     * Retrieves the [TableInfo] object for the given table class or object.
     *
     * @param obj The table class or object for which to retrieve the [TableInfo].
     * @return The TableInfo object for the given table class or object.
     * @throws SerializerException if the table info for the table cannot be found.
     */
    internal fun <T : Any> getTableInfo(obj: T): TableInfo = this.getTableInfo(kclass = obj::class)

    /**
     * Decodes the result set into a list of objects of the specified output classes.
     *
     * @param resultSet The result set to decode.
     * @param i The index of the output class in the vararg list.
     * @param outputs The vararg list of output classes.
     * @return The list of decoded objects.
     * @throws SerializerException If there are missing output classes.
     */
    internal fun decodeMany(resultSet: ResultSet, i: Int, vararg outputs: KClass<*>): List<Any> {
        val output = outputs.getOrNull(i)
        val objs = mutableListOf<Any>()

        if (output != null) while (resultSet.next()) objs.add(this.decode(resultSet, output))
        else throw SerializerException("Missing output classes, because there are more queries listed in the query: ${outputs.map { it.simpleName }}")

        return objs
    }

    /**
     * Decodes the given [ResultSet] into an object of the specified class [T].
     *
     * @param resultSet The [ResultSet] to decode.
     * @param kclass The [KClass] representing the desired class of the decoded object.
     * @return The decoded object of type [T].
     * @throws MapperException if there is an error during decoding.
     */
    internal fun <T : Any> decode(resultSet: ResultSet, kclass: KClass<T>): T {

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
