package com.urosjarc.dbmessiah.domain.queries

class BatchQuery(
    val sql: String,
    val valueMatrix: List<List<QueryValue>>
)
