package com.urosjarc.dbmessiah.data

import com.urosjarc.dbmessiah.extend.*
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

    /**
     * Represents whether a column should generate an autoUUID value.
     *
     * This property is used to determine if a column in a database table should generate an auto-generated UUID value.
     * The value is true if the column is configured to generate an autoUUID, and false otherwise.
     *
     * @return true if the column should generate an autoUUID value, false otherwise
     */
    public val autoUUID: Boolean
        get() {
            val isWholeNumber = kprop.ext_isUUID || kprop.ext_isInlineUUID
            val flags = listOf(isWholeNumber, kprop.ext_isMutable, kprop.returnType.isMarkedNullable)
            return false !in flags
        }
}
