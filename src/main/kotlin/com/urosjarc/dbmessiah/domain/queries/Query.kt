package com.urosjarc.dbmessiah.domain.queries

import com.urosjarc.dbmessiah.domain.columns.Column

data class Query(
    override val sql: String,
    override val values: List<QueryValue> = listOf()
) : Unsafe
