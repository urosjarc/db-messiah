package com.urosjarc.dbmessiah.domain.columns

import com.urosjarc.dbmessiah.domain.serialization.Decoder
import com.urosjarc.dbmessiah.domain.serialization.Encoder
import java.sql.JDBCType
import kotlin.reflect.KMutableProperty1

class PrimaryColumn(
    val autoIncrement: Boolean,
    override val kprop: KMutableProperty1<Any, Any?>,
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
