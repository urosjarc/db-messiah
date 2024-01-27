package com.urosjarc.dbmessiah.domain.queries

class InsertQuery(
    sql: String,
    values: List<QueryValue> = listOf()
) : Unsafe(sql = sql, values = values)
