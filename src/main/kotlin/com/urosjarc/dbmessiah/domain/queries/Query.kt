package com.urosjarc.dbmessiah.domain.queries

class Query(
    sql: String,
    values: List<QueryValue> = listOf()
) : Unsafe(sql = sql, values = values)
