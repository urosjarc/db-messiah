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
    public val primaryKey: KProperty1<T, *>,
    public val foreignKeys: List<Pair<KProperty1<T, *>, KClass<*>>> = listOf(),
    internal var constraints: List<Pair<KProperty1<T, *>, List<C>>> = listOf(),
    internal val serializers: List<TypeSerializer<*>> = listOf(),
    internal val columnSerializers: List<Pair<KProperty1<T, *>, TypeSerializer<*>>> = listOf()
) {
    init {
        val pkConstraints = this.constraints.firstOrNull { it.first == this.primaryKey }?.second
        val pkCanBeNull = this.primaryKey.returnType.isMarkedNullable

        /**
         * LOGIC FOR THE PRIMARY KEYS
         * Constraint tests will be present in mapper tests.
         */

        if (pkCanBeNull) { // Column marked as auto incremental...
            if (pkConstraints != null) { // User defined his own primary key constraints...
                if (!pkConstraints.contains(C.AUTO_INC)) {
                    // User forgot to add auto incremental constraint for the primary key...
                    this.constraints += listOf(Pair(this.primaryKey, listOf(C.AUTO_INC)))
                    // User did not define primary key constraints
                } else this.constraints = listOf(Pair(this.primaryKey, listOf(C.AUTO_INC)))
            }
        }

        /**
         * LOGIC FOR THE FOREIGN KEYS
         */

        this.foreignKeys.forEach {
            val fk = it.first
            val fkConstraints = it.second
            val fkCanBeNull = fk.returnType.isMarkedNullable
            val fkIsMutable = fk is KMutableProperty1<T, *>

            throw Exception("You stayed here!")

            if (pkCanBeNull) { // Fk Column marked as null...
                if (pkConstraints != null) { // User defined his own primary key constraints...
                    if (pkConstraints.contains(C.NOT_NULL)) { // User added auto incremental constraint for the primary key...
                        //Everything good user did not forget to add auto incremental constraints :)
                    } else { // Primary key constraints does not contains auto incremental...
                        //User forgot to add auto increment to the constraints and we shall warn him about it...
                        throw SerializerException("User defined constraints missing AUTO_INC constraint for mutable primary key: $primaryKey")
                    }
                } else { // User did not defined primary key constraints...
                    // We create primary key constraints with auto incremental...
                    this.constraints = listOf(Pair(this.primaryKey, listOf(C.AUTO_INC)))
                }

            } else { // Column marked non auto incremental...
                if (pkConstraints != null) { // User defined his own primary key constraints...
                    if (pkConstraints.contains(C.AUTO_INC)) { // User add auto incremental constraint for the primary key...
                        //User add redundant auto increment constraint to the constraints and we shall warn him about it...
                        throw SerializerException("User defined constraints contains redundant AUTO_INC constraint for imutable primary key: $primaryKey")
                    } else { // Primary key constraints does not contains auto incremental...
                        // All good
                    }
                } else { // User did not defined primary key constraints...
                    // All good
                }
            }

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
