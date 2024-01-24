package com.urosjarc.dbjesus.domain

import java.sql.JDBCType

interface Unsafe {
    val sql: String
    val encoders: MutableList<Encoder<Any>>
    val values: MutableList<Any?>
    val jdbcTypes: MutableList<JDBCType>
}
