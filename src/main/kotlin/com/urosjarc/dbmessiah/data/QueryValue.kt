package com.urosjarc.dbmessiah.data

import java.sql.JDBCType

/**
 * Represents a SQL [QueryValue] to be used in a JDBC prepared statement.
 * This [QueryValue] will replace question marks in the final SQL statement.
 *
 * @property name The name of the value.
 * @property value The actual value.
 * @property jdbcType The JDBC data type of the value.
 * @property encoder The [Encoder] used to serialize the value.
 */
public data class QueryValue(
    public val name: String,
    public val value: Any?,
    public val jdbcType: JDBCType,
    public val encoder: Encoder<*>
) {


    /**
     * Represents [value] that will be escaped with quotations.
     */
    public val escapped: String
        get() = when (this.value) {
            is Int -> value.toString()
            is UInt -> value.toString()
            else -> {
                try { // TODO: Fix this
                    "'$value'"
                } catch (e: NullPointerException){
                    "NULL"
                }
            }
        }

    /** @suppress */
    override fun hashCode(): Int = name.hashCode()//OK

    /** @suppress */
    override fun toString(): String {
        return "$name: ($jdbcType,$escapped)"
    }

    /** @suppress */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is QueryValue) return false
        return name == other.name
    }


}
