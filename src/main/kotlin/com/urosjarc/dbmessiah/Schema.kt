package com.urosjarc.dbmessiah

import com.urosjarc.dbmessiah.data.TypeSerializer
import com.urosjarc.dbmessiah.domain.Table
import kotlin.reflect.KClass

/**
 * Represents a user defined database schema.
 *
 * @property name The name of the schema.
 * @property tables A list of tables in the schema.
 * @property serializers A list of type serializers for the schema.
 * @property procedures A list of procedures contained in the schema.
 * @constructor Creates a new Schema instance.
 */
public open class Schema(
    public val name: String,
    internal var tables: List<Table<*>>,
    internal val serializers: List<TypeSerializer<out Any>> = listOf(),
    internal val procedures: List<KClass<*>> = listOf()
) {

    /** @suppress */

    /** @suppress */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Schema) return false
        return name == other.name
    }

    /** @suppress */
    override fun toString(): String = "'${this.name}'"
}
