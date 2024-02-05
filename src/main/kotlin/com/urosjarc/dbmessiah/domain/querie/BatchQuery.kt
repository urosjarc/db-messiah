package com.urosjarc.dbmessiah.domain.querie

class BatchQuery(
    val sql: String,
    val valueMatrix: List<List<QueryValue>>
)
