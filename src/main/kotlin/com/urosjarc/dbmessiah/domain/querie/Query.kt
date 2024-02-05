package com.urosjarc.dbmessiah.domain.querie

class Query(
    val sql: String,
    vararg val values: QueryValue
) {
    private val hash = "$sql$values".hashCode()
    override fun hashCode(): Int = this.hash
    override fun equals(other: Any?): Boolean {
        return this.hashCode() == other.hashCode()
    }

    override fun toString(): String {
        var index = 1
        val values = if (values.isEmpty()) "" else "\n" + values.joinToString(separator = "") {
            val value = when (it.value) {
                is String -> "'${it.value}'"
                else -> it.value.toString()
            }
            "\t${index++}) ${it.name}: ${it.jdbcType} = ${value}\n"
        }
        return "\n\n$sql\n${values}"
    }
}
