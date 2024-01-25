package com.urosjarc.dbjesus.domain

import java.sql.JDBCType

interface Unsafe {
    val sql: String
    val encoders: MutableList<Encoder<*>>
    val values: MutableList<*>
    val jdbcTypes: MutableList<JDBCType>
}
