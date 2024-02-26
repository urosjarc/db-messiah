package com.urosjarc.dbmessiah.data

import com.urosjarc.dbmessiah.extend.ext_canBeNull
import com.urosjarc.dbmessiah.extend.ext_isMutable
import com.urosjarc.dbmessiah.extend.ext_isWholeNumber
import java.sql.JDBCType
import kotlin.reflect.KMutableProperty1

/**
 * Represents a column in a [TableInfo], specifically a primary column.
 *
 * @property getAutoIncrement Determines if the primary column has auto-increment enabled.
 */
internal class PrimaryColumn(
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
) {

    val autoIncrement: Boolean get() = false !in listOf(kprop.ext_isWholeNumber, kprop.ext_isMutable, kprop.ext_canBeNull)
}
