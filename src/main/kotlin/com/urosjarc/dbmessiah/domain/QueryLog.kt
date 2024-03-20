package com.urosjarc.dbmessiah.domain

import kotlin.time.Duration

public data class QueryLog(
    val type: Type,
    val duration: Duration,
    val sql: String,
    var repetitions: Int
) {
    public enum class Type { BATCH, UPDATE, INSERT, EXECUTE, QUERY, CALL }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as QueryLog
        if (type != other.type) return false
        if (sql != other.sql) return false
        return true
    }

    override fun hashCode(): Int = 31 * type.hashCode() + sql.hashCode()


}
