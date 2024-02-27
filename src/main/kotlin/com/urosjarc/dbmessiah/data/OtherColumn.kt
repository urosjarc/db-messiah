package com.urosjarc.dbmessiah.data

import java.sql.JDBCType
import kotlin.reflect.KProperty1

/**
 * Represents an other column in a [TableInfo].
 *
 * @property unique Determines whether the column values should be unique.
 */
public open class OtherColumn(
    public val unique: Boolean,
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
     * If values in this column can be null or not.
     */
    public val notNull: Boolean = !this.kprop.returnType.isMarkedNullable
}
