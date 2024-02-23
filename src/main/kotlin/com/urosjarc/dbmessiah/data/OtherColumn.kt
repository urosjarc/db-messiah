package com.urosjarc.dbmessiah.data

import java.sql.JDBCType
import kotlin.reflect.KProperty1

internal open class OtherColumn(
    val unique: Boolean,
    kprop: KProperty1<Any, Any?>,
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
) {
    val notNull: Boolean get() = !this.kprop.returnType.isMarkedNullable
}
