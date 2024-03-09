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
    public val autoInc: Boolean = kprop.ext_isAutoInc

    /**
     * Represents whether a column should generate an auto-generated UUID value.
     */
    public val autoUUID: Boolean = kprop.ext_isAutoUUID
}
