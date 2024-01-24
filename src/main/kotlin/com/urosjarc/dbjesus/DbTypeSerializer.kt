package com.urosjarc.dbjesus

import com.urosjarc.dbjesus.domain.Decoder
import com.urosjarc.dbjesus.domain.Encoder
import java.sql.JDBCType
import kotlin.reflect.KClass

class DbTypeSerializer<T : Any>(
    val kclass: KClass<T>,
    val dbType: String,
    val jdbcType: JDBCType,
    val decoder: Decoder<T>,
    val encoder: Encoder
)
