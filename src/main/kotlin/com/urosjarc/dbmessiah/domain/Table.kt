package com.urosjarc.dbmessiah.domain

import com.urosjarc.dbmessiah.data.TypeSerializer
import com.urosjarc.dbmessiah.exceptions.MappingException
import com.urosjarc.dbmessiah.exceptions.SerializerTestsException
import com.urosjarc.dbmessiah.extend.ext_notUnique
import com.urosjarc.dbmessiah.extend.ext_owner
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.full.extensionReceiverParameter
import kotlin.reflect.full.instanceParameter

/**
 * A class representing a table in a database or schema.
 * This class is intermediate structure created by the user which is
 * then converted by the [Mapper] to more appropriate [TableInfo] structure.
 *
 * @param T the type that represents database table.
 * @property primaryKey the primary key of the table.
 * @property serializers the serializers which will overrides global and schema serializer that will help serialize table columns (optional).
 */
public class Table<T : Any>(
    public val primaryKey: KProperty1<T, *>,
    foreignKeys: List<Pair<KProperty1<T, *>, KClass<*>>> = listOf(),
    constraints: List<Pair<KProperty1<T, *>, List<C>>> = listOf(),
    internal val serializers: List<TypeSerializer<*>> = listOf(),
    columnSerializers: List<Pair<KProperty1<T, *>, TypeSerializer<*>>> = listOf()
) {

    /**
     * Unescaped name of the [Table].
     */
    public val name: String =
        ((primaryKey.instanceParameter ?: primaryKey.extensionReceiverParameter)!!.type.classifier as KClass<*>).simpleName.toString()

    /**
     * Map of [Table] foreign keys that points to specific foreign class.
     */
    public val foreignKeys: Map<KProperty1<T, *>, KClass<*>>

    /**
     * Map of [Table] column constraints.
     */
    internal val constraints: Map<KProperty1<T, *>, List<C>>

    /**
     * Represents a map of column serializers.
     * This serializer have the highest priority over all other serializers defined
     * inside database serializer.
     */
    internal val columnSerializers: Map<KProperty1<T, *>, TypeSerializer<*>>

    init {
        val fkDuplicates = foreignKeys.map { it.first }.ext_notUnique
        val constDuplicates = constraints.map { it.first }.ext_notUnique
        val columnSerializersDuplicates = columnSerializers.map { it.first }.ext_notUnique

        if (fkDuplicates.isNotEmpty())
            throw SerializerTestsException("Table $name has foreign keys registered multiple times: $fkDuplicates")

        if (constDuplicates.isNotEmpty())
            throw SerializerTestsException("Table $name has constraints registered multiple times: $constDuplicates")

        if (columnSerializersDuplicates.isNotEmpty())
            throw SerializerTestsException("Table $name has column serializers registered multiple times: $columnSerializersDuplicates")

        this.foreignKeys = foreignKeys.toMap()
        this.constraints = constraints.toMap()
        this.columnSerializers = columnSerializers.toMap()
    }

    /**
     * Kotlin class that represents this [Table].
     */
    public val kclass: KClass<*> = primaryKey.ext_owner ?: throw SerializerTestsException("Could not found enclosing class for primary key: $primaryKey"))

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
