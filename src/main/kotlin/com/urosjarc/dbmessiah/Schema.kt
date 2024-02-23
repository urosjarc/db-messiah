package com.urosjarc.dbmessiah

import com.urosjarc.dbmessiah.domain.Table
import com.urosjarc.dbmessiah.data.TypeSerializer
import kotlin.reflect.KClass

public open class Schema(
    internal val name: String,
    internal var tables: List<Table<*>>,
    internal val serializers: List<TypeSerializer<out Any>> = listOf(),
    internal val procedures: List<KClass<*>> = listOf()
) {
    private val hash = this.name.hashCode()
    override fun hashCode(): Int = this.hash

    override fun equals(other: Any?): Boolean {
        return this.hashCode() == other.hashCode()
    }
    override fun toString(): String = "'${this.name}'"
}
