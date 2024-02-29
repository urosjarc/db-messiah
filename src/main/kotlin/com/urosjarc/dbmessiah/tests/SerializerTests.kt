package com.urosjarc.dbmessiah.tests

import com.urosjarc.dbmessiah.Mapper
import com.urosjarc.dbmessiah.Schema
import com.urosjarc.dbmessiah.Serializer
import com.urosjarc.dbmessiah.data.TypeSerializer
import com.urosjarc.dbmessiah.domain.C
import com.urosjarc.dbmessiah.domain.Table
import com.urosjarc.dbmessiah.exceptions.MappingException
import com.urosjarc.dbmessiah.exceptions.SerializerTestsException
import com.urosjarc.dbmessiah.extend.ext_notUnique
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.jvm.javaField

/**
 * Responsible for testing the user defined configuration.
 * Tests are responsible to provide strong type safety before database initialization.
 * It contains various tests to ensure the correctness of the database configuration.
 *
 * @param ser The [Mapper] instance to be tested.
 */
internal class SerializerTests {

    class EmptinessTests(val ser: Serializer) {

        /**
         * Root level
         */

        fun `At least one table must exist`() {
            this.ser.schemas.getOrNull(0)?.tables?.getOrNull(0) ?: throw SerializerTestsException("Missing schema or it has no registered table")
        }

        fun `Global inputs must have at least 1 property and primary constructor with required non-optional parameters`() {
            this.ser.globalInputs.forEach {
                val ki = KClassInfo(it)
                ki.constructor ?: throw SerializerTestsException("Global input $it must have primary constructor")
                if (ki.kparams.isEmpty()) throw SerializerTestsException("Global input $it must have at least one non-optional primary constructor parameter")
                if (ki.kprops.isEmpty()) throw SerializerTestsException("Global input $it must have at least one property")
                if (ki.optionalKParams.isNotEmpty()) throw SerializerTestsException("Global input $it must have primary constructor with non-optional parameters: ${ki.optionalKParams}")
            }
        }

        fun `Global outputs must have at least 1 property and primary constructor with required non-optional parameters`() {
            this.ser.globalOutputs.forEach {
                val ki = KClassInfo(it)
                ki.constructor ?: throw SerializerTestsException("Global output $it must have primary constructor")
                if (ki.kparams.isEmpty()) throw SerializerTestsException("Global output $it must have at least one non-optional primary constructor parameter")
                if (ki.kprops.isEmpty()) throw SerializerTestsException("Global output $it must have at least one property.")
                if (ki.optionalKParams.isNotEmpty()) throw SerializerTestsException("Global output $it must have primary constructor with non-optional parameters: ${ki.optionalKParams}")
            }
        }

        fun `Global procedures must have primary constructor with non-optional parameters`() {
            this.ser.globalProcedures.forEach {
                val ki = KClassInfo(it)
                ki.constructor ?: throw SerializerTestsException("Global procedure $it must have primary constructor")
                if (ki.optionalKParams.isNotEmpty()) throw SerializerTestsException("Global procedure $it must have primary constructor with non-optional parameters: ${ki.optionalKParams}")
            }
        }

        /**
         * Schema level
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
         * Table level
         */
        fun `Tables must have non-empty primary constructor`() {
            this.ser.schemas.forEach { schema: Schema ->
                schema.tables.forEach {
                    val ki = KClassInfo(it.kclass)
                    ki.constructor ?: throw SerializerTestsException("Table ${schema.name}.$it must have primary constructor")
                    if (ki.optionalKParams.isNotEmpty()) throw SerializerTestsException("Global procedure ${schema.name}.$it must have primary constructor with non-optional parameters: ${ki.optionalKParams}")
                }
            }
        }

    }

    class UniquenessTests(val ser: Serializer) {
        /**
         * Root level uniquness
         */
        fun `Schemas must be unique`() {
            val notUnique = this.ser.schemas.ext_notUnique
            if (notUnique.isNotEmpty()) throw SerializerTestsException("Schemas registered multiple times: ${notUnique.keys}")
        }

        fun `Global serializers must be unique`() {
            val notUnique = this.ser.globalSerializers.ext_notUnique
            if (notUnique.isNotEmpty()) throw SerializerTestsException("Global serializers registered multiple times: ${notUnique.keys}")
        }

        fun `Global inputs must be unique`() {
            val notUnique = this.ser.globalInputs.ext_notUnique
            if (notUnique.isNotEmpty()) throw SerializerTestsException("Global inputs registered multiple times: ${notUnique.keys}")
        }

        fun `Global outputs must be unique`() {
            val notUnique = this.ser.globalOutputs.ext_notUnique
            if (notUnique.isNotEmpty()) throw SerializerTestsException("Global outputs registered multiple times: ${notUnique.keys}")
        }

        fun `Global procedures must be unique`() {
            val notUnique = this.ser.globalProcedures.ext_notUnique
            if (notUnique.isNotEmpty()) throw SerializerTestsException("Global procedures registered multiple times: ${notUnique.keys}")
        }

        /**
         * Schema level uniqueness
         */

        fun `Schemas serializers must be unique`() {
            this.ser.schemas.forEach {
                val notUnique = it.serializers.ext_notUnique
                if (notUnique.isNotEmpty()) throw SerializerTestsException("Schema $it has serializers registered multiple times: ${notUnique.keys}")
            }
        }

        fun `Schemas procedures must be unique`() {
            this.ser.schemas.forEach {
                val notUnique = it.procedures.ext_notUnique
                if (notUnique.isNotEmpty()) throw SerializerTestsException("Schema $it has procedures registered multiple times: ${notUnique.keys}")
            }
        }

        /**
         * Table level uniqueness, other tests are located directly inside table init.
         */
        fun `Tables must be unique`() {
            this.ser.schemas.forEach {
                val notUnique = it.tables.ext_notUnique
                if (notUnique.isNotEmpty()) throw SerializerTestsException("Schema $it has tables registered multiple times: ${notUnique.keys} ")
            }
        }

        fun `Tables serializers keys must be unique`() {
            this.ser.schemas.forEach { schema ->
                schema.tables.forEach { table ->
                    val notUnique = table.serializers.ext_notUnique
                    if (notUnique.isNotEmpty()) throw SerializerTestsException("Table $schema.$table has serializers registered multiple times: ${notUnique.keys}")
                }
            }
        }

        /**
         * Column level uniqueness
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

    class TableTests(val ser: Serializer) {
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
                                        C.UNIQUE -> throw MappingException("Primary key ${schema}.${table}.'${table.primaryKey.name}' does not need '${C.UNIQUE}' constraint")
                                        C.CASCADE_UPDATE -> throw MappingException("Primary key ${schema}.${table}.'${table.primaryKey.name}' does not need '${C.CASCADE_UPDATE}' constraint")
                                        C.CASCADE_DELETE -> throw MappingException("Primary key ${schema}.${table}.'${table.primaryKey.name}' does not need '${C.CASCADE_DELETE}' constraint")
                                    }
                                }
                        }
                    }
                }
            }
        }
    }

    class SerializationTests(val ser: Serializer) {
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

    private class KClassInfo(kclass: KClass<*>) {
        val constructor = kclass.primaryConstructor
        val kparams: List<KParameter> = kclass.primaryConstructor?.parameters?.filter { it.kind == KParameter.Kind.VALUE } ?: listOf()
        val kprops: List<KProperty1<out Any, *>> = kclass.memberProperties.filter { it.javaField != null }
        val optionalKParams = kclass.primaryConstructor?.parameters?.filter { p -> p.isOptional }?.map { "'${it.name}'" } ?: listOf()
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

        private fun hasSerializer(kparam: KParameter, typeSerializers: List<TypeSerializer<*>>): Boolean =
            typeSerializers.map { it.kclass }.contains(kparam.type.classifier as KClass<*>)

        private fun hasSerializer(kprop: KProperty1<*, *>, typeSerializers: List<TypeSerializer<*>>): Boolean =
            typeSerializers.map { it.kclass }.contains(kprop.returnType.classifier as KClass<*>)
    }
}
