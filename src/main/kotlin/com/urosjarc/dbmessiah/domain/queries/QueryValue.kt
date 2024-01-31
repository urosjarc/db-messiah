package com.urosjarc.dbmessiah.domain.queries

import com.urosjarc.dbmessiah.domain.serialization.Encoder
import java.sql.JDBCType

data class QueryValue(
    val name: String,
    val value: Any?,
    val jdbcType: JDBCType,
    val encoder: Encoder<*>
) {

    val hash = this.toString().hashCode()

    override fun toString(): String {
        return "$name: ($jdbcType,$escapped)"
    }

    override fun hashCode(): Int = this.hash

    override fun equals(other: Any?): Boolean {
        return this.hashCode() == other.hashCode()
    }

    val escapped
        get() = when (this.value) {
            is String -> "'$value'"
            is Char -> "'$value'"
            else -> this.value.toString()
        }

}
