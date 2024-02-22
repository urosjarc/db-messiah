package com.urosjarc.dbmessiah.domain.querie

public class BatchQuery(
    public val sql: String,
    public val valueMatrix: List<List<QueryValue>>
)
