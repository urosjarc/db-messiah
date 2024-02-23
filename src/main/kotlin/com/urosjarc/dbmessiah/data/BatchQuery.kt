package com.urosjarc.dbmessiah.data

/**
 * A class representing a batch query.
 *
 * @property sql The SQL query string.
 * @property valueMatrix The matrix of [QueryValue], every row represents list of [QueryValue] for one [Query].
 */
public class BatchQuery(
    public val sql: String,
    public val valueMatrix: List<List<QueryValue>>
)
