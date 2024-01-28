package com.urosjarc.dbmessiah.domain.queries

class Query(
    val sql: String,
    val values: List<QueryValue> = listOf()
) {
    override fun toString(): String {
        var index = 1
        val values = if (values.isEmpty()) "" else "\n" + values.joinToString(separator = "")  {
            val value = when(it.value){
                is String -> "'${it.value}'"
                else -> it.value.toString()
            }
            "\t${index++}) ${it.name}: ${it.jdbcType} = ${value}\n"
        }
        return "$sql\n${values}"
    }
}
