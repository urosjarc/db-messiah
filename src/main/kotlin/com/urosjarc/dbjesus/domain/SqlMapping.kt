package com.urosjarc.dbjesus.domain

import java.sql.JDBCType
import java.sql.ResultSet
import kotlin.reflect.KClass

class SqlMapping<T : Any>(
    val kclass: KClass<T>,
    val dbType: String,
    val jdbcType: JDBCType,
    val decoding: (i: Int, rs: ResultSet) -> T?,
    val encoding: (obj: T) -> String
)
