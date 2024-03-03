package com.urosjarc.dbmessiah.tests

import com.urosjarc.dbmessiah.Mapper
import com.urosjarc.dbmessiah.Schema
import com.urosjarc.dbmessiah.Serializer
import com.urosjarc.dbmessiah.data.TypeSerializer
import com.urosjarc.dbmessiah.domain.C
import com.urosjarc.dbmessiah.domain.Table
import com.urosjarc.dbmessiah.exceptions.SerializerTestsException
import com.urosjarc.dbmessiah.extend.ext_isMutable
import com.urosjarc.dbmessiah.extend.ext_notUnique
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.jvm.javaField

/**
 * Responsible for testing the user defined [Serializer] configuration.
 * Tests are responsible to provide strong type safety before database initialization.
 * It contains various tests to ensure the correctness of the database configuration.
 *
 * @param ser The [Mapper] instance to be tested.
 */
internal class SerializerTests {

    /**
     * Class for testing the emptiness of different components in the Serializer.
     *
     * @property ser The instance of the Serializer class being tested.
     */
    class EmptinessTests(val ser: Serializer) {

        /**
         * Checks if at least one table exists in the first schema.
         * Throws [SerializerTestsException] if either the schema is missing
         * or it has no registered table.
         */
        fun `At least one table must exist`() {
            this.ser.schemas.getOrNull(0)?.tables?.getOrNull(0) ?: throw SerializerTestsException("Missing schema or it has no registered table")
        }

        /**
         * This method checks if the global inputs have the necessary properties and primary constructor with required non-optional parameters.
         *
         * @throws SerializerTestsException if any global input does not meet the requirements
         */
        fun `Global inputs must have at least 1 property and primary constructor with required non-optional parameters`() {
            this.ser.globalInputs.forEach {
                val ki = KClassInfo(it)
                ki.constructor ?: throw SerializerTestsException("Global input $it must have primary constructor")
                if (ki.kparams.isEmpty()) throw SerializerTestsException("Global input $it must have at least one non-optional primary constructor parameter")
                if (ki.kprops.isEmpty()) throw SerializerTestsException("Global input $it must have at least one property")
                if (ki.optionalKParams.isNotEmpty()) throw SerializerTestsException("Global input $it must have primary constructor with non-optional parameters: ${ki.optionalKParams}")
            }
        }

        /**
         * Checks if all global outputs have the necessary properties and primary constructor with required non-optional parameters.
         * Throws a [SerializerTestsException] if any global output does not meet the requirements.
         */
        fun `Global outputs must have at least 1 property and primary constructor with required non-optional parameters`() {
            this.ser.globalOutputs.forEach {
                val ki = KClassInfo(it)
                ki.constructor ?: throw SerializerTestsException("Global output $it must have primary constructor")
                if (ki.kparams.isEmpty()) throw SerializerTestsException("Global output $it must have at least one non-optional primary constructor parameter")
                if (ki.kprops.isEmpty()) throw SerializerTestsException("Global output $it must have at least one property.")
                if (ki.optionalKParams.isNotEmpty()) throw SerializerTestsException("Global output $it must have primary constructor with non-optional parameters: ${ki.optionalKParams}")
            }
        }

        /**
         * Global procedures must have a primary constructor with non-optional parameters.
         * Throws a [SerializerTestsException] if any global procedure does not meet the requirements.
         */
        fun `Global procedures must have primary constructor with non-optional parameters`() {
            this.ser.globalProcedures.forEach {
                val ki = KClassInfo(it)
                ki.constructor ?: throw SerializerTestsException("Global procedure $it must have primary constructor")
                if (ki.optionalKParams.isNotEmpty()) throw SerializerTestsException("Global procedure $it must have primary constructor with non-optional parameters: ${ki.optionalKParams}")
            }
        }

        /**
         * Validates that all procedures in the given schemas have a primary constructor with non-optional parameters.
         * If any procedure does not meet this requirement, a [SerializerTestsException] is thrown.
         */
        fun `Schemas procedures must have primary constructor with non-optional parameters`() {
            this.ser.schemas.forEach { schema: Schema ->
                schema.procedures.forEach {
                    val ki = KClassInfo(it)
                    ki.constructor ?: throw SerializerTestsException("Global procedure ${schema.name}.$it must have primary constructor")
                    if (ki.optionalKParams.isNotEmpty()) throw SerializerTestsException("Global procedure ${schema.name}.$it must have primary constructor with non-optional parameters: ${ki.optionalKParams}")
                }
            }
        }

        /**
         * Checks if tables have a non-empty primary constructor and columns.
         *
         * @throws SerializerTestsException If a table does not have a primary constructor or if it has an empty primary constructor or if it does not have any columns.
         */
        fun `Tables must have non-empty primary constructor and columns`() {
            this.ser.schemas.forEach { schema: Schema ->
                schema.tables.forEach {
                    val ki = KClassInfo(it.kclass)
                    ki.constructor ?: throw SerializerTestsException("Table ${schema.name}.$it must have primary constructor")
                    if (ki.kparams.isEmpty()) throw SerializerTestsException("Table ${schema.name}.$it must have non-empty primary constructor")
                    if (ki.kprops.isEmpty()) throw SerializerTestsException("Table ${schema.name}.$it must have columns")
                }
            }
        }
    }

    /**
     * Class for performing uniqueness tests on a Serializer object.
     * User should not provide serializer with duplications.
     *
     * @property ser The Serializer object to perform tests on.
     */
    class UniquenessTests(val ser: Serializer) {

        /**
         * Checks if schemas are unique.
         */
        fun `Schemas must be unique`() {
            val notUnique = this.ser.schemas.ext_notUnique
            if (notUnique.isNotEmpty()) {
                throw SerializerTestsException("Schemas registered multiple times: ${notUnique.keys}")
            }
        }

        /**
         * Checks if global serializers are unique.
         *
         * This method verifies that the global serializers registered in the system are unique. If any serializers are
         * registered multiple times, a [SerializerTestsException] is thrown with the list of the duplicate serializers.
         *
         * @throws SerializerTestsException if global serializers are registered multiple times
         */
        fun `Global serializers must be unique`() {
            val notUnique = this.ser.globalSerializers.ext_notUnique
            if (notUnique.isNotEmpty()) throw SerializerTestsException("Global serializers registered multiple times: ${notUnique.keys}")
        }

        /**
         * Checks if global inputs are unique.
         *
         * This method verifies that the global inputs registered in the system are unique.
         * If any inputs are registered multiple times, a [SerializerTestsException] is thrown with the list of the duplicate inputs.
         *
         * @throws SerializerTestsException if global inputs are registered multiple times
         */
        fun `Global inputs must be unique`() {
            val notUnique = this.ser.globalInputs.ext_notUnique
            if (notUnique.isNotEmpty()) throw SerializerTestsException("Global inputs registered multiple times: ${notUnique.keys}")
        }

        /**
         * Verifies that the global outputs registered in the system are unique.
         * If any outputs are registered multiple times, a [SerializerTestsException] is thrown with the list of the duplicate outputs.
         *
         * @throws SerializerTestsException if global outputs are registered multiple times
         */
        fun `Global outputs must be unique`() {
            val notUnique = this.ser.globalOutputs.ext_notUnique
            if (notUnique.isNotEmpty()) throw SerializerTestsException("Global outputs registered multiple times: ${notUnique.keys}")
        }

        /**
         * Checks if global procedures are unique.
         *
         * This method verifies that the global procedures registered in the system are unique.
         * If any procedures are registered multiple times, a [SerializerTestsException] is thrown with the list of the duplicate procedures.
         *
         * @throws SerializerTestsException if global procedures*/
        fun `Global procedures must be unique`() {
            val notUnique = this.ser.globalProcedures.ext_notUnique
            if (notUnique.isNotEmpty()) throw SerializerTestsException("Global procedures registered multiple times: ${notUnique.keys}")
        }

        /**
         * Verifies that the serializers for each schema are unique.
         *
         * @throws SerializerTestsException if any serializers are registered multiple times for a schema
         */
        fun `Schemas serializers must be unique`() {
            this.ser.schemas.forEach {
                val notUnique = it.serializers.ext_notUnique
                if (notUnique.isNotEmpty()) throw SerializerTestsException("Schema $it has serializers registered multiple times: ${notUnique.keys}")
            }
        }

        /**
         * Checks if the procedures for each schema are unique.
         *
         * @throws SerializerTestsException if any procedures are registered multiple times for a schema
         */
        fun `Schemas procedures must be unique`() {
            this.ser.schemas.forEach {
                val notUnique = it.procedures.ext_notUnique
                if (notUnique.isNotEmpty()) throw SerializerTestsException("Schema $it has procedures registered multiple times: ${notUnique.keys}")
            }
        }

        /**
         * Checks if tables in each schema are unique.
         *
         * This method iterates over all schemas and checks if any tables are registered multiple times.
         * If any tables are found to be registered multiple times, a [SerializerTestsException] is thrown
         * with the list of duplicate tables.
         *
         * @throws SerializerTestsException if tables are registered multiple times in any schema
         */
        fun `Tables must be unique`() {
            this.ser.schemas.forEach {
                val notUnique = it.tables.ext_notUnique
                if (notUnique.isNotEmpty()) throw SerializerTestsException("Schema $it has tables registered multiple times: ${notUnique.keys} ")
            }
        }

        /**
         * Checks if the keys of serializers registered for each table in the schemas are unique.
         *
         * @throws SerializerTestsException if any serializers are registered multiple times for a table
         */
        fun `Tables serializers keys must be unique`() {
            this.ser.schemas.forEach { schema ->
                schema.tables.forEach { table ->
                    val notUnique = table.serializers.ext_notUnique
                    if (notUnique.isNotEmpty()) throw SerializerTestsException("Table $schema.$table has serializers registered multiple times: ${notUnique.keys}")
                }
            }
        }

        /**
         * Checks that column constraints are unique.
         *
         * This method iterates over all schemas, tables, and column constraints to verify
         * that each column constraint is registered only once. If any column constraints are
         * found to be registered multiple times, a [SerializerTestsException] is thrown with
         * the list of duplicate column constraints.
         *
         * @throws SerializerTestsException if column constraints are registered multiple times
         */
        fun `Column constraints must be unique`() {
            this.ser.schemas.forEach { schema ->
                schema.tables.forEach { table ->
                    table.constraints.forEach { kprop, constraints: List<C> ->
                        val notUnique = constraints.ext_notUnique
                        if (notUnique.isNotEmpty()) throw SerializerTestsException("Column $schema.$table.${kprop.name} has constraints registered multiple times: ${notUnique.keys}")
                    }
                }
            }
        }
    }

    /**
     * Class for testing various constraints on table objects.
     *
     * @property ser The serializer used for testing.
     */
    class TableTests(val ser: Serializer) {
        /**
         * Checks if the primary keys of all tables in the given schema are not both immutable and optional.
         * If any table violates this constraint, a [SerializerTestsException] is thrown.
         */
        fun `Tables primary keys must not be imutable and optional at the same time`() {
            this.ser.schemas.forEach { schema ->
                schema.tables.forEach { table ->
                    if (table.primaryKey.returnType.isMarkedNullable && !table.primaryKey.ext_isMutable)
                        throw SerializerTestsException("Primary key must not be imutable and optional at the same time: ${table.primaryKey}")
                }
            }
        }

        /**
         * Checks if the foreign keys of all tables do not contain the primary key.
         * If any table violates this constraint, a [SerializerTestsException] is thrown.
         */
        fun `Tables foreign keys must not contain primary key`() {
            this.ser.schemas.forEach { schema ->
                schema.tables.forEach { table ->
                    if (table.foreignKeys.keys.contains(table.primaryKey))
                        throw SerializerTestsException("Foreign keys must not contain primary key for table: $table")
                }
            }
        }

        /**
         * Tables foreign keys must point to registered table with primary key of same type.
         * This method checks if all foreign keys in the tables of the given schema point to registered tables with primary keys of the same type.
         * If any foreign key violates this constraint, a [SerializerTestsException] is thrown.
         */
        fun `Tables foreign keys must point to registered table with primary key of same type`() {
            val schemas_tables = mutableListOf<Pair<Schema, Table<*>>>()

            this.ser.schemas.forEach { schema ->
                schema.tables.forEach { table ->
                    schemas_tables.add(Pair(first = schema, second = table))
                }
            }
            this.ser.schemas.forEach { schema ->
                schema.tables.forEach { table ->
                    table.foreignKeys.forEach { fk ->

                        val schema_fkTable = schemas_tables.firstOrNull { fk.value == it.second.kclass }
                            ?: throw SerializerTestsException("Foreign key ${schema}.${table}.${fk.key.name} points to unregistered table '${fk.value.simpleName}'")

                        val fkKClass = fk.key.returnType.classifier as KClass<*>
                        val pkKClass = schema_fkTable.second.primaryKey.returnType.classifier as KClass<*>
                        if (pkKClass != fkKClass)
                            throw SerializerTestsException(
                                "Foreign key ${schema}.${table}.${fk.key.name} is of type '${fkKClass.simpleName}' but it points to primary key " +
                                        "${schema_fkTable.first}.${schema_fkTable.second}.'${schema_fkTable.second.primaryKey.name}' that is of different type: ${pkKClass.simpleName}"
                            )
                    }
                }
            }
        }

        /**
         * Validates the constraints of the tables for specific columns.
         *
         * This function iterates over each schema, table, and constraint in the provided database.
         * It checks if the constraint is applicable for the specific column in the table.
         * If it's not applicable, it throws a [SerializerTestsException] with a corresponding error message.
         *
         * @throws SerializerTestsException If any constraint is invalid for a specific column.
         */
        fun `Tables constraints must be valid for specific column`() {
            this.ser.schemas.forEach { schema ->
                schema.tables.forEach { table ->
                    table.constraints.forEach { prop_const ->

                        val prop = prop_const.key

                        //Primary key does not need constraints
                        if (prop == table.primaryKey) {
                            if (prop_const.value.isNotEmpty())
                                prop_const.value.forEach {
                                    when (it) {
                                        C.UNIQUE -> throw SerializerTestsException("Primary key ${schema}.${table}.'${table.primaryKey.name}' does not need '${C.UNIQUE}' constraint")
                                        C.CASCADE_UPDATE -> throw SerializerTestsException("Primary key ${schema}.${table}.'${table.primaryKey.name}' does not need '${C.CASCADE_UPDATE}' constraint")
                                        C.CASCADE_DELETE -> throw SerializerTestsException("Primary key ${schema}.${table}.'${table.primaryKey.name}' does not need '${C.CASCADE_DELETE}' constraint")
                                    }
                                }
                        }
                    }
                }
            }
        }
    }

    /**
     * A class that tests if objects can be serializable.
     *
     * @param ser The serializer to be tested.
     */
    class SerializationTests(val ser: Serializer) {
        /**
         * Checks if all database values have exactly one matching type serializer.
         *
         * @param ser The serializer to be tested.
         */
        fun `All database values must have excactly one matching type serializer`() {
            //Schema level
            this.ser.schemas.forEach { s ->
                s.tables.forEach { t -> KClassInfo(t.kclass).check(t.serializers + s.serializers + this.ser.globalSerializers, t.columnSerializers) }
                s.procedures.forEach { KClassInfo(it).check(s.serializers + this.ser.globalSerializers) }
            }
            //Root level
            this.ser.globalInputs.forEach { KClassInfo(it).check(this.ser.globalSerializers) }
            this.ser.globalOutputs.forEach { KClassInfo(it).check(this.ser.globalSerializers) }
            this.ser.globalProcedures.forEach { KClassInfo(it).check(this.ser.globalSerializers) }
        }
    }

    /**
     * Represents a class that provides information about a Kotlin class.
     * This class is used inside [SerializerTests] methods.
     *
     * @property constructor The primary constructor of the class.
     * @property kparams The list of primary constructor parameters that are of kind VALUE.
     * @property kprops The list of member properties that have associated java fields.
     * @property optionalKParams The list of optional primary constructor parameters.
     */
    private class KClassInfo(kclass: KClass<*>) {
        val constructor = kclass.primaryConstructor
        val kparams: List<KParameter> = kclass.primaryConstructor?.parameters?.filter { it.kind == KParameter.Kind.VALUE } ?: listOf()
        val kprops: List<KProperty1<out Any, *>> = kclass.memberProperties.filter { it.javaField != null }
        val optionalKParams = kclass.primaryConstructor?.parameters?.filter { p -> p.isOptional }?.map { "'${it.name}'" } ?: listOf()
        /**
         * Checks if all properties and parameters have matching type serializers.
         *
         * @param typeSerializers The list of type serializers used to encode and decode values.
         * @param columnSerializers The map of column serializers used to encode and decode values for specific columns.
         *         Defaults to an empty map if not provided.
         * @throws SerializerTestsException if any property or parameter does not have a matching type serializer.
         */
        fun check(
            typeSerializers: List<TypeSerializer<*>>,
            columnSerializers: Map<out KProperty1<out Any, Any?>, TypeSerializer<out Any>> = mapOf()
        ) {
            this.kprops.forEach {
                if (!hasSerializer(it, typeSerializers + listOf(columnSerializers[it]).filterNotNull()))
                    throw SerializerTestsException("Property $it does not have matching type serializer of type: ${it.returnType}")
            }
            this.kparams.forEach {
                if (!hasSerializer(it, typeSerializers))
                    throw SerializerTestsException("Parameter $it does not have matching type serializer of type: ${it.type}")
            }
        }

        /**
         * Checks if the given [kparam] has a matching type serializer in the [typeSerializers] list.
         *
         * @param kparam The parameter to check for a matching type serializer.
         * @param typeSerializers The list of type serializers used to encode and decode values.
         * @return true if the [kparam] has a matching type serializer, false otherwise.
         */
        private fun hasSerializer(kparam: KParameter, typeSerializers: List<TypeSerializer<*>>): Boolean =
            typeSerializers.map { it.kclass }.contains(kparam.type.classifier as KClass<*>)

        /**
         * Checks if the given [kprop] has a matching type serializer in the [typeSerializers] list.
         *
         * @param kprop The property to check for a matching type serializer.
         * @param typeSerializers The list of type serializers used to encode and decode values.
         * @return true if the [kprop] has a matching type serializer, false otherwise.
         */
        private fun hasSerializer(kprop: KProperty1<*, *>, typeSerializers: List<TypeSerializer<*>>): Boolean =
            typeSerializers.map { it.kclass }.contains(kprop.returnType.classifier as KClass<*>)
    }
}
