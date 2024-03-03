package com.urosjarc.dbmessiah.data

import com.urosjarc.dbmessiah.extend.ext_isInlineWholeNumber
import com.urosjarc.dbmessiah.extend.ext_isMutable
import com.urosjarc.dbmessiah.extend.ext_isWholeNumber
import java.sql.JDBCType
import kotlin.reflect.KProperty1

/**
 * Represents [PrimaryColumn] in a [TableInfo].
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

    /**
     * Represents whether a column is auto-increment.
     */
    public val autoInc: Boolean
        get() {
            val isWholeNumber = kprop.ext_isWholeNumber || kprop.ext_isInlineWholeNumber
            val flags = listOf(isWholeNumber, kprop.ext_isMutable, kprop.returnType.isMarkedNullable)
            return false !in flags
        }
}
