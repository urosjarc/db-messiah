package com.urosjarc.dbmessiah.data

import java.sql.JDBCType
import kotlin.reflect.KProperty1

/**
 * Represents an other column in a [TableInfo].
 *
 * @property unique Determines whether the column values should be unique.
 */
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

    /**
     * If values in this column can be null or not.
     */
    val notNull: Boolean get() = !this.kprop.returnType.isMarkedNullable
}
