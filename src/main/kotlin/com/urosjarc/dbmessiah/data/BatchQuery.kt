package com.urosjarc.dbmessiah.data

public class BatchQuery(
    public val sql: String,
    public val valueMatrix: List<List<QueryValue>>
)
