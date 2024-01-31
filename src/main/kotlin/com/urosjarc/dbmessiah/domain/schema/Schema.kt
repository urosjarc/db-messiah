package com.urosjarc.dbmessiah.domain.schema

import com.urosjarc.dbmessiah.domain.serialization.TypeSerializer
import com.urosjarc.dbmessiah.domain.table.Table

class Schema(
    val name: String,
    val serializers: List<TypeSerializer<out Any>> = listOf(),
    var tables: List<Table<*>>,
) {
    val hash = this.name.hashCode()
    override fun hashCode(): Int = this.hash

    override fun equals(other: Any?): Boolean {
        return this.hashCode() == other.hashCode()
    }
    override fun toString(): String = "'${this.name}'"
}
