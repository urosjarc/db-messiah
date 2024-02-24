package com.urosjarc.dbmessiah.domain

import com.urosjarc.dbmessiah.data.TypeSerializer
import com.urosjarc.dbmessiah.exceptions.SerializerException
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty1
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
    public val primaryKey: KMutableProperty1<out T, out Any?>,
    public val foreignKeys: List<Pair<KProperty1<T, *>, KClass<*>>> = listOf(),
    internal var constraints: List<Pair<KProperty1<T, *>, List<C>>> = listOf(),
    internal val serializers: List<TypeSerializer<*>> = listOf(),
    internal val columnSerializers: List<Pair<KProperty1<T, *>, TypeSerializer<*>>> = listOf()
) {
    init {
        //If user has not provided constraints for primary key, then add default ones here
        this.primaryKey as KProperty1<T, *>
        if (!this.constraints.map { it.first }.contains(this.primaryKey)) {
            val constrs = this.constraints.toMutableList()
            constrs.add(Pair(first = this.primaryKey, second = listOf(C.AUTO_INC)))
            this.constraints = constrs
        }
    }

    /**
     * The name of the table.
     */
    public val name: String =
        ((primaryKey.instanceParameter ?: primaryKey.extensionReceiverParameter)!!.type.classifier as KClass<*>).simpleName.toString()

    /**
     * Kotlin class that represents this [Table].
     */
    public val kclass: KClass<*> =
        (primaryKey.javaField?.declaringClass?.kotlin ?: throw SerializerException("Could not found enclosing class for primary key"))

    /**
     * Returns a list of primary key constraints associated with the table.
     */
    internal val primaryKeyConstraints get() = this.constraintsFor(kprop = this.primaryKey)

    /**
     * Retrieves the constraints for the given property that is associated in the table column.
     */
    internal fun constraintsFor(kprop: KProperty1<*, *>) = this.constraints.firstOrNull { it.first == kprop }?.second ?: listOf()

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
