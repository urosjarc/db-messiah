package com.urosjarc.dbmessiah.domain

import com.urosjarc.dbmessiah.data.TypeSerializer
import com.urosjarc.dbmessiah.exceptions.MappingException
import com.urosjarc.dbmessiah.exceptions.SerializerTestsException
import com.urosjarc.dbmessiah.extend.ext_notUnique
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.full.extensionReceiverParameter
import kotlin.reflect.full.instanceParameter
import kotlin.reflect.jvm.javaField

/**
 * A class representing a table in a database or schema.
 *
 * @param T the type that represents database table.
 * @property primaryKey the primary key of the table.
 * @property foreignKeys the foreign keys of the table that points to foreign table (optional).
 * @property constraints the constraints of the table (optional).
 * @property serializers the serializers which will overrides global and schema serializer that will help serialize table columns (optional).
 * @property columnSerializers the serializers which will overrides global, schema and table serializers (optional).
 */
public class Table<T : Any>(
    public val primaryKey: KProperty1<T, *>,
    foreignKeys: List<Pair<KProperty1<T, *>, KClass<*>>> = listOf(),
    constraints: List<Pair<KProperty1<T, *>, List<C>>> = listOf(),
    internal val serializers: List<TypeSerializer<*>> = listOf(),
    columnSerializers: List<Pair<KProperty1<T, *>, TypeSerializer<*>>> = listOf()
) {

    public val name: String =
        ((primaryKey.instanceParameter ?: primaryKey.extensionReceiverParameter)!!.type.classifier as KClass<*>).simpleName.toString()


    public val foreignKeys: Map<KProperty1<T, *>, KClass<*>>
    internal val constraints: Map<KProperty1<T, *>, List<C>>
    internal val columnSerializers: Map<KProperty1<T, *>, TypeSerializer<*>>

    init {
        val fkDuplicates = foreignKeys.map { it.first }.ext_notUnique
        val constDuplicates = constraints.map { it.first }.ext_notUnique
        val columnSerializersDuplicates = columnSerializers.map { it.first }.ext_notUnique

        if (fkDuplicates.isEmpty())
            throw SerializerTestsException("Table $name has foreign keys registered multiple times: $fkDuplicates")

        if (constDuplicates.isEmpty())
            throw SerializerTestsException("Table $name has constraints registered multiple times: $constDuplicates")

        if (columnSerializersDuplicates.isEmpty())
            throw SerializerTestsException("Table $name has column serializers registered multiple times: $columnSerializersDuplicates")

        this.foreignKeys = foreignKeys.toMap()
        this.constraints = constraints.toMap()
        this.columnSerializers = columnSerializers.toMap()
    }

    /**
     * Kotlin class that represents this [Table].
     */
    public val kclass: KClass<*> =
        (primaryKey.javaField?.declaringClass?.kotlin ?: throw MappingException("Could not found enclosing class for primary key"))

    /** @suppress */
    private val hash = this.name.hashCode()

    /** @suppress */
    override fun hashCode(): Int = this.hash

    /** @suppress */
    override fun equals(other: Any?): Boolean {
        return this.hashCode() == other.hashCode()
    }

    /** @suppress */
    override fun toString(): String = "'${this.name}'"

}
