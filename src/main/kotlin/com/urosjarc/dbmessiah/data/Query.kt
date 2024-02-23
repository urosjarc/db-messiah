package com.urosjarc.dbmessiah.data

/**
 * A class representing a SQL query and its values.
 *
 * @param sql The SQL query template that may contains JDBC question marks.
 * @param values The array of [QueryValue] objects to be used in [sql] template as question marks replacement.
 */
public class Query(
    public var sql: String,
    public vararg val values: QueryValue
) {
    /** @suppress */
    private val hash = "$sql$values".hashCode()

    /** @suppress */
    override fun hashCode(): Int = this.hash

    /** @suppress */
    override fun equals(other: Any?): Boolean {
        return this.hashCode() == other.hashCode()
    }

    /** @suppress */
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
