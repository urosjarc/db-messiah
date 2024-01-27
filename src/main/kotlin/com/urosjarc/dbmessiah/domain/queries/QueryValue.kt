package com.urosjarc.dbmessiah.domain.queries

import com.urosjarc.dbmessiah.domain.serialization.Encoder
import java.sql.JDBCType

data class QueryValue(
    val name: String,
    val value: Any?,
    val jdbcType: JDBCType,
    val encoder: Encoder<*>
) {
    override fun toString(): String {
        val strVal = when(this.value){
            is String -> "'$value'"
            is Char -> "'$value'"
            else -> this.value
        }
        return "$name: ($jdbcType,$strVal)"
    }
}
