package com.urosjarc.dbmessiah.data

import com.urosjarc.dbmessiah.extend.ext_isOptional
import java.sql.JDBCType
import kotlin.reflect.KProperty1

/**
 * Represents an ordinary column that is not [PrimaryColumn] nor [ForeignColumn].
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
    public val notNull: Boolean = !this.kprop.ext_isOptional
}
