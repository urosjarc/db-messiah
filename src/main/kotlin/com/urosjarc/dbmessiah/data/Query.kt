package com.urosjarc.dbmessiah.data

/**
 * A class representing a SQL query and its values.
 *
 * @param sql The SQL query template that may contains JDBC question marks.
 * @param values The array of [QueryValue] objects to be used in [sql] template as question marks replacements.
 */
public class Query(
    public var sql: String,
    public vararg val values: QueryValue
) {

    /** @suppress */
    override fun hashCode(): Int = 31 * sql.hashCode() + values.contentHashCode()//OK

    /** @suppress */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Query) return false
        if (sql != other.sql) return false
        if (!values.contentEquals(other.values)) return false
        return true
    }

    /** @suppress */
    override fun toString(): String {
        var index = 1
        val values = if (values.isEmpty()) "" else "\n" + values.joinToString(separator = "") {
            "\t${index++}) ${it.name}: ${it.jdbcType} = ${it.escapped}\n"
        }
        return "\n\n$sql\n${values}"
    }

}
