package com.urosjarc.dbmessiah

import com.urosjarc.dbmessiah.domain.serialization.TypeSerializer
import com.urosjarc.dbmessiah.domain.table.Table
import kotlin.reflect.KClass

open class Schema(
    val name: String,
    var tables: List<Table<*>>,
    val serializers: List<TypeSerializer<out Any>> = listOf(),
    val procedures: List<KClass<*>> = listOf()
) {
    val hash = this.name.hashCode()
    override fun hashCode(): Int = this.hash

    override fun equals(other: Any?): Boolean {
        return this.hashCode() == other.hashCode()
    }
    override fun toString(): String = "'${this.name}'"
}
