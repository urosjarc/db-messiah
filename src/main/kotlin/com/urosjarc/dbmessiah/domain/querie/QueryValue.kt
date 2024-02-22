package com.urosjarc.dbmessiah.domain.querie

import com.urosjarc.dbmessiah.domain.serialization.Encoder
import java.sql.JDBCType

public data class QueryValue(
    internal val name: String,
    internal val value: Any?,
    internal val jdbcType: JDBCType,
    internal val encoder: Encoder<*>
) {

    private val hash = this.toString().hashCode()

    override fun toString(): String {
        return "$name: ($jdbcType,$escapped)"
    }

    override fun hashCode(): Int = this.hash

    override fun equals(other: Any?): Boolean {
        return this.hashCode() == other.hashCode()
    }

    internal val escapped
        get() = when (this.value) {
            is String -> "'$value'"
            is Char -> "'$value'"
            else -> this.value.toString()
        }

}
