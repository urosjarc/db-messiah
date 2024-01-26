package com.urosjarc.dbjesus.domain.queries

import com.urosjarc.dbjesus.domain.serialization.Encoder
import java.sql.JDBCType

data class InsertQuery(
    override val sql: String,
    override val encoders: MutableList<Encoder<*>>,
    override val values: MutableList<*>,
    override val jdbcTypes: MutableList<JDBCType>
) : Unsafe
