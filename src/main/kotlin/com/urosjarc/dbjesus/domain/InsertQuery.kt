package com.urosjarc.dbjesus.domain

import java.sql.JDBCType

data class InsertQuery(
    override val sql: String,
    override val encoders: MutableList<Encoder<Any>>,
    override val values: MutableList<Any?>,
    override val jdbcTypes: MutableList<JDBCType>
) : Unsafe
