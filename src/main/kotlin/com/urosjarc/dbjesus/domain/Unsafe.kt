package com.urosjarc.dbjesus.domain

import com.urosjarc.dbjesus.exceptions.QueryException
import java.sql.JDBCType

interface Unsafe {
    val sql: String
    val encoders: MutableList<Encoder<Any>>
    val values: MutableList<Any?>
    val jdbcTypes: MutableList<JDBCType>
}
