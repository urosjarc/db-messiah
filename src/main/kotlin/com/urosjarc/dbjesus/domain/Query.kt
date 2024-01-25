package com.urosjarc.dbjesus.domain

import java.sql.JDBCType

data class Query(
    override val sql: String,
    override val encoders: MutableList<Encoder<Any>> = mutableListOf(),
    override val values: MutableList<Any?> = mutableListOf(),
    override val jdbcTypes: MutableList<JDBCType> = mutableListOf()
) : Unsafe
