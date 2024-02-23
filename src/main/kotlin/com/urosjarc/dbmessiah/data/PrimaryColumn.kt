package com.urosjarc.dbmessiah.data

import java.sql.JDBCType
import kotlin.reflect.KMutableProperty1

internal class PrimaryColumn(
    val autoIncrement: Boolean,
    kprop: KMutableProperty1<Any, Any?>,
    dbType: String,
    jdbcType: JDBCType,
    encoder: Encoder<*>,
    decoder: Decoder<*>
) : Column(
    kprop = kprop,
    dbType = dbType,
    jdbcType = jdbcType,
    encoder = encoder,
    decoder = decoder
)
