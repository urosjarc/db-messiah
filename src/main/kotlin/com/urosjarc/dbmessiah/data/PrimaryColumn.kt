package com.urosjarc.dbmessiah.data

import com.urosjarc.dbmessiah.extend.ext_canBeNull
import com.urosjarc.dbmessiah.extend.ext_isInlineWholeNumber
import com.urosjarc.dbmessiah.extend.ext_isMutable
import com.urosjarc.dbmessiah.extend.ext_isWholeNumber
import java.sql.JDBCType
import kotlin.reflect.KProperty1

/**
 * Represents a column in a [TableInfo], specifically a primary column.
 *
 * @property getAutoIncrement Determines if the primary column has auto-increment enabled.
 */
public class PrimaryColumn(
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

    public val autoInc: Boolean = false !in listOf(kprop.ext_isWholeNumber || kprop.ext_isInlineWholeNumber, kprop.ext_isMutable, kprop.ext_canBeNull)
}
