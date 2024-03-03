package com.urosjarc.dbmessiah.data

/**
 * Represents a [BatchQuery] to be executed on a database. It consists of an SQL string and a value matrix of [QueryValue].
 * One row of [QueryValue] matrix represents values for one database call.
 *
 * @property sql The SQL string for the [BatchQuery].
 * @property valueMatrix The value matrix containing the query values to be used in the prepared statement.
 */
public class BatchQuery(
    public val sql: String,
    public val valueMatrix: List<List<QueryValue>>
)
