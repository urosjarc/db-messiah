package com.urosjarc.dbmessiah.tests

import com.urosjarc.dbmessiah.domain.columns.C
import com.urosjarc.dbmessiah.domain.schema.Schema
import com.urosjarc.dbmessiah.domain.serialization.TypeSerializer
import com.urosjarc.dbmessiah.domain.table.Table
import com.urosjarc.dbmessiah.exceptions.SerializerException
import com.urosjarc.dbmessiah.extend.ext_javaFields
import com.urosjarc.dbmessiah.extend.ext_kclass
import com.urosjarc.dbmessiah.extend.ext_notUnique
import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor

class TestSerializer(val schemas: List<Schema>, val globalSerializers: List<TypeSerializer<*>>, val inputs: List<KClass<*>>, val outputs: List<KClass<*>>) {
    /**
     * CHECK FOR EMPTYNESS
     */
    fun `1-th Test - If at least one table exist`() {
        this.schemas.getOrNull(0)?.tables?.getOrNull(0) ?: throw SerializerException("Schema has no registered table")
    }

    /**
     * SCHEMA OBJECTS REGISTERING MULTIPLE TIMES
     */
    fun `2-th Test - If schema registered multiple times`() {
        val notUnique = this.schemas.map { it.name }.ext_notUnique
        if (notUnique.isNotEmpty()) throw SerializerException("Schemas registered multiple times: ${notUnique.keys}")
    }

    fun `3-th Test - If schemas table registered multiple times`() {
        this.schemas.forEach {
            val notUnique = it.tables.map { it.kclass }.ext_notUnique
            if (notUnique.isNotEmpty()) throw SerializerException("Schema '${it.name}' has tables ${notUnique.keys} registered multiple times")
        }
    }

    fun `4-th Test - If schemas table foreign key registered multiple times`() {
        this.schemas.forEach { schema ->
            schema.tables.forEach { table ->
                val notUnique = table.foreignKeys.map { it.first }.ext_notUnique
                if (notUnique.isNotEmpty()) throw SerializerException("Table '${schema.name}.${table.name}' has foreign keys ${notUnique.keys} registered multiple times")
            }
        }
    }

    fun `5-th Test - If schemas table constraints registered multiple times`() {
        this.schemas.forEach { schema ->
            schema.tables.forEach { table ->
                val notUnique = table.constraints.map { it.first }.ext_notUnique
                if (notUnique.isNotEmpty()) throw SerializerException("Table '${schema.name}.${table.name}' has constraints ${notUnique.keys} registered multiple times")
            }
        }
    }

    /**
     * SERIALIZERS REGISTERED MULTIPLE TIMES
     */
    fun `6-th Test - If schema serializers registered multiple times`() {
        this.schemas.forEach {
            val notUnique = it.serializers.ext_notUnique
            if (notUnique.isNotEmpty()) throw SerializerException("Schema '${it.name}' has serializers ${notUnique.keys} registered multiple times")
        }
    }

    fun `7-th Test - If schemas table serializers registered multiple times`() {
        this.schemas.forEach { schema ->
            schema.tables.forEach { table ->
                val notUnique = table.serializers.ext_notUnique
                if (notUnique.isNotEmpty()) throw SerializerException("Table '${schema.name}.${table.name}' has serializers ${notUnique.keys} registered multiple times")
            }
        }
    }

    /**
     * MAP VALUES ARE CORRECT
     */
    fun `8-th Test - If schemas table foreign keys are pointing to registered table with the same type`() {
        val schemas_tables = mutableListOf<Pair<Schema, Table<*>>>()

        this.schemas.forEach { schema ->
            schema.tables.forEach { table ->
                schemas_tables.add(Pair(first = schema, second = table))
            }
        }
        this.schemas.forEach { schema ->
            schema.tables.forEach { table ->
                table.foreignKeys.forEach { fk ->

                    val end_schema_table = schemas_tables.firstOrNull { fk.second == it.second.kclass }
                        ?: throw SerializerException("Foreign key '${schema.name}.${table.name}.${fk.first.name}' points to unregistered class '${fk.second.simpleName}'")

                    if (end_schema_table.second.primaryKey.ext_kclass != fk.first.ext_kclass)
                        throw SerializerException(
                            "Foreign key '${schema.name}.${table.name}.${fk.first.name}' is type of '${fk.first.ext_kclass.simpleName}' " +
                                    "but it points to incompatible primary key '${end_schema_table.first.name}.${end_schema_table.second.name}.${end_schema_table.second.primaryKey.name}'"
                        )
                }
            }
        }
    }

    fun `9-th Test - If schemas table constraints are valid`() {
        this.schemas.forEach { schema ->
            schema.tables.forEach { table ->
                table.constraints.forEach { prop_const ->

                    val prop = prop_const.first

                    //Primary key
                    if (prop == table.primaryKey) {
                        prop_const.second.forEach {
                            when (it) {
                                C.AUTO_INC -> {}
                                C.UNIQUE -> throw SerializerException("Primary key '${schema.name}.${table.name}.${table.primaryKey.name}' does not need ${C.UNIQUE} constraint")
                            }
                        }
                    }

                    //Foreign constraints
                    if (table.foreignKeys.map { it.first }.contains(prop)) {
                        prop_const.second.forEach {
                            when (it) {
                                C.AUTO_INC -> throw SerializerException("Foreign key '${schema.name}.${table.name}.${prop.name}' does not need ${C.AUTO_INC} constraint")
                                C.UNIQUE -> {}
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * EVERY TABLE COLUMN MUST HAV APPROPRIATE SERIALIZER
     */
    fun `10-th Test - If schemas objects have appropriate serializer`() {
        this.schemas.forEach { schema ->
            schema.tables.forEach { table ->
                val allSerializers = (this.globalSerializers + schema.serializers + table.serializers).map { it.kclass }
                table.kclass.ext_javaFields.forEach {
                    if (!allSerializers.contains(it.ext_kclass))
                        throw SerializerException("Property '${schema.name}.${table.name}.${it.name}' of type '${it.ext_kclass.simpleName} does not have appropriate serializer")
                }
            }
        }
    }

    /**
     * Every input objects has registered serializers
     */
    fun `11-th Test - If all input classes properties have appropriate serializer`() {
        this.inputs.forEach { ik ->
            ik.ext_javaFields.forEach { ip ->
                this.globalSerializers.firstOrNull { it.kclass == ip.ext_kclass }
                    ?: throw SerializerException("Input property '${ik.simpleName}.${ip.name}' with type '${ip.ext_kclass}' does not have appropriate serializer")
            }
        }
    }

    /**
     * Every input objects has registered serializers
     */
    fun `12-th Test - If all primary keys that are autoincrement have integer dbType`() {
        this.schemas.forEach { schema ->
            schema.tables.forEach { table ->
                if (table.primaryKeyConstraints.contains(C.AUTO_INC)) {
                    if (table.primaryKey.ext_kclass != Int::class) {
                        throw SerializerException("Primary key '${schema.name}.${table.name}.${table.primaryKey.name}' of type '${table.primaryKey.ext_kclass.simpleName}' has constrain 'AUTO_INC' but then it should be of type 'Int'")
                    }
                }

            }
        }
    }

    fun `13-th Test - If all input classes have imutable and not null properties`() {
        this.inputs.forEach { input ->
            input.ext_javaFields.forEach { kp ->
                if (kp.returnType.isMarkedNullable) {
                    throw SerializerException("Input property '${input.simpleName}.${kp.name}' can be null which is not allowed on any input class!")
                }
            }
        }
    }

    fun `14-th Test - If all output classes have no default values`() {
        this.outputs.forEach { input ->
            val conParams = (input.primaryConstructor?.parameters ?: emptyList()).filter { p -> p.isOptional }.map { it.name }
            if (conParams.isNotEmpty())
                throw SerializerException("Output class '${input.simpleName}' have primary constructor with optional arguments $conParams, which is not allowed on any output class!")
        }
    }

}