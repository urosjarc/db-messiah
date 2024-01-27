package com.urosjarc.dbmessiah.domain.queries

abstract class Unsafe(val sql: String, val values: List<QueryValue>) {
    override fun toString(): String {
        val values = if (values.isEmpty()) "" else "\n" + values.joinToString(separator = "") {
            val value = when(it.value){
                is String -> "'${it.value}'"
                else -> it.value.toString()
            }
            "\t - ${it.name}: ${it.jdbcType} = ${value}\n"
        }
        return "$sql\n${values}"
    }
}
