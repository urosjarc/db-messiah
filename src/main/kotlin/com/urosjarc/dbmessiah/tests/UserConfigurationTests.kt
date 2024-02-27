package com.urosjarc.dbmessiah.tests

import com.urosjarc.dbmessiah.Mapper
import com.urosjarc.dbmessiah.Schema
import com.urosjarc.dbmessiah.domain.C
import com.urosjarc.dbmessiah.domain.Table
import com.urosjarc.dbmessiah.exceptions.SerializingException
import com.urosjarc.dbmessiah.extend.ext_notUnique
import kotlin.reflect.KClass

/**
 * Responsible for testing the user defined configuration.
 * Tests are responsible to provide strong type safety before database initialization.
 * It contains various tests to ensure the correctness of the database configuration.
 *
 * @param mapper The [Mapper] instance to be tested.
 */
internal class UserConfigurationTests(val mapper: Mapper) {
    /**
     * CHECK FOR EMPTYNESS
     */
    fun `1-th Test - If at least one table exist`() {
        this.mapper.schemas.getOrNull(0)?.tables?.getOrNull(0) ?: throw SerializingException("Missing schema or it has no registered table")
    }

    /**
     * SCHEMA OBJECTS REGISTERING MULTIPLE TIMES
     */
    fun `2-th Test - If schema registered multiple times`() {
        val notUnique = this.mapper.schemas.ext_notUnique
        if (notUnique.isNotEmpty()) throw SerializingException("Schemas registered multiple times: ${notUnique.keys}")
    }

    fun `3-th Test - If schemas table registered multiple times`() {
        this.mapper.schemas.forEach {
            val notUnique = it.tables.ext_notUnique
            if (notUnique.isNotEmpty()) throw SerializingException("Schema $it has tables ${notUnique.keys} registered multiple times")
        }
    }

    /**
     * SERIALIZERS REGISTERED MULTIPLE TIMES
     */
    fun `6-th Test - If schema serializers registered multiple times`() {
        this.mapper.schemas.forEach {
            val notUnique = it.serializers.ext_notUnique
            if (notUnique.isNotEmpty()) throw SerializingException("Schema ${it} has serializers ${notUnique.keys} registered multiple times")
        }
    }

    fun `7-th Test - If schemas table serializers registered multiple times`() {
        this.mapper.schemas.forEach { schema ->
            schema.tables.forEach { table ->
                val notUnique = table.serializers.ext_notUnique
                if (notUnique.isNotEmpty()) throw SerializingException("Table ${schema}.${table} has serializers ${notUnique.keys} registered multiple times")
            }
        }
    }

    /**
     * MAP VALUES ARE CORRECT
     */
    fun `8-th Test - If schemas table foreign keys are pointing to wrong table`() {
        val schemas_tables = mutableListOf<Pair<Schema, Table<*>>>()

        this.mapper.schemas.forEach { schema ->
            schema.tables.forEach { table ->
                schemas_tables.add(Pair(first = schema, second = table))
            }
        }
        this.mapper.schemas.forEach { schema ->
            schema.tables.forEach { table ->
                table.foreignKeys.forEach { fk ->

                    val schema_fkTable = schemas_tables.firstOrNull { fk.value == it.second.kclass }
                        ?: throw SerializingException("Foreign key ${schema}.${table}.'${fk.key.name}' points to unregistered class '${fk.value.simpleName}'")

                    val fkKClass = fk.key.returnType.classifier as KClass<*>
                    if (schema_fkTable.second.primaryKey.returnType.classifier != fkKClass)
                        throw SerializingException(
                            "Foreign key ${schema}.${table}.'${fk.key.name}' is type of '${fkKClass.simpleName}' " +
                                    "but it points to incompatible primary key ${schema_fkTable.first}.${schema_fkTable.second}.'${schema_fkTable.second.primaryKey.name}'"
                        )
                }
            }
        }
    }

    fun `9-th Test - If schemas table constraints are valid`() {
        this.mapper.schemas.forEach { schema ->
            schema.tables.forEach { table ->
                table.constraints.forEach { prop_const ->

                    val prop = prop_const.key

                    //Primary key
                    if (prop == table.primaryKey) {
                        prop_const.value.forEach {
                            when (it) {
                                C.UNIQUE -> throw SerializingException("Primary key ${schema}.${table}.'${table.primaryKey.name}' does not need '${C.UNIQUE}' constraint")
                                C.CASCADE_UPDATE -> throw SerializingException("Primary key ${schema}.${table}.'${table.primaryKey.name}' does not need '${C.CASCADE_UPDATE}' constraint")
                                C.CASCADE_DELETE -> throw SerializingException("Primary key ${schema}.${table}.'${table.primaryKey.name}' does not need '${C.CASCADE_DELETE}' constraint")
                            }
                        }
                    }

                    //Foreign constraints
                    if (table.foreignKeys.map { it.key }.contains(prop)) {
                        prop_const.value.forEach {
                            when (it) {
                                C.UNIQUE -> {}
                                C.CASCADE_UPDATE -> {}
                                C.CASCADE_DELETE -> {}
                            }
                        }
                    }
                }
            }
        }
    }


    fun `12-th Test - If all output classes have no default values`() {
        this.mapper.globalOutputs.forEach { input ->
            val constructor = this.mapper.getConstructor(kclass = input)
            val defaultParams = constructor.parameters.filter { p -> p.isOptional }.map { "'${it.name}'" }
            if (defaultParams.isNotEmpty())
                throw SerializingException("Output class '${input.simpleName}' have primary constructor with optional arguments $defaultParams, which is not allowed on any output class!")
        }
    }

    /**
     * GLOBAL OBJECTS REGISTERING MULTIPLE TIMES
     */
    fun `13-th Test - If global procedure registered multiple times`() {
        val notUnique = this.mapper.globalProcedures.ext_notUnique
        if (notUnique.isNotEmpty()) throw SerializingException("Global procedures registered multiple times: ${notUnique.keys}")
    }

    fun `14-th Test - If global outputs registered multiple times`() {
        val notUnique = this.mapper.globalOutputs.ext_notUnique
        if (notUnique.isNotEmpty()) throw SerializingException("Global outputs registered multiple times: ${notUnique.keys}")
    }

    fun `15-th Test - If global inputs registered multiple times`() {
        val notUnique = this.mapper.globalInputs.ext_notUnique
        if (notUnique.isNotEmpty()) throw SerializingException("Global inputs registered multiple times: ${notUnique.keys}")
    }

    fun `16-th Test - If global serializers registered multiple times`() {
        val notUnique = this.mapper.globalInputs.ext_notUnique
        if (notUnique.isNotEmpty()) throw SerializingException("Global serializers registered multiple times: ${notUnique.keys}")
    }

}
