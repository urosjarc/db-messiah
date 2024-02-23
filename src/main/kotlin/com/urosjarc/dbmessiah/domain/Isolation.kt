package com.urosjarc.dbmessiah.domain

/**
 * An enum class representing the isolation levels for database transactions.
 * This enum was created only to hide HikariCP API!
 *
 * @property levelId The ID of the isolation level.
 */
public enum class Isolation(public val levelId: Int) {
    NONE(0),
    READ_UNCOMMITTED(1),
    READ_COMMITTED(2),
    CURSOR_STABILITY(3),
    REPEATABLE_READ(4),
    LAST_COMMITTED(5),
    SERIALIZABLE(8),
    SQL_SERVER_SNAPSHOT(4096)
}
