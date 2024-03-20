package com.urosjarc.dbmessiah.domain

import kotlin.time.Duration

/**
 * Represents a query log entry.
 *
 * This class holds information about a specific query log entry, including the type of query,
 * the duration of the query execution, the SQL statement, and the number of repetitions.
 *
 * @param type The type of query.
 * @param duration The duration of the query execution.
 * @param sql The SQL statement.
 * @param repetitions The number of times the query was repeated.
 */
public data class QueryLog(
    val type: Type,
    var duration: Duration,
    val sql: String,
    var repetitions: Int
) {
    /**
     * Enum class representing type of [QueryLog]
     */
    public enum class Type { BATCH, UPDATE, INSERT, EXECUTE, QUERY, CALL }

    /** @suppress */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as QueryLog
        if (type != other.type) return false
        if (sql != other.sql) return false
        return true
    }

    /** @suppress */
    override fun hashCode(): Int = 31 * type.hashCode() + sql.hashCode()


}
