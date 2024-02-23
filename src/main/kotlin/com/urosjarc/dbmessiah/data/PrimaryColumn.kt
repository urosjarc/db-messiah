package com.urosjarc.dbmessiah.data

import java.sql.JDBCType
import kotlin.reflect.KMutableProperty1

/**
 * Represents a column in a [TableInfo], specifically a primary column.
 *
 * @property autoIncrement Determines if the primary column has auto-increment enabled.
 */
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
