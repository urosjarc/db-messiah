package com.urosjarc.dbjesus.domain.queries

import com.urosjarc.dbjesus.domain.serialization.Encoder
import java.sql.JDBCType

interface Unsafe {
    val sql: String
    val encoders: MutableList<Encoder<*>>
    val values: MutableList<*>
    val jdbcTypes: MutableList<JDBCType>
}
