package com.urosjarc.dbmessiah.domain.queries

data class InsertQuery(
    override val sql: String,
    override val values: List<QueryValue>,
) : Unsafe
