package com.urosjarc.dbmessiah.data

import java.sql.JDBCType
import kotlin.reflect.KProperty1

/**
 * Represents a foreign column table.
 *
 * @property cascadeDelete Determines whether the delete operation cascades to the foreign table.
 * @property cascadeUpdate Determines whether the update operation cascades to the foreign table.
 * @property foreignTable The [TableInfo] of the foreign table.
 */
internal class ForeignColumn(
    unique: Boolean,
    val cascadeDelete: Boolean,
    val cascadeUpdate: Boolean,
    kprop: KProperty1<Any, Any?>,
    dbType: String,
    jdbcType: JDBCType,
    encoder: Encoder<*>,
    decoder: Decoder<*>
) : OtherColumn(
    unique = unique,
    kprop = kprop,
    dbType = dbType,
    jdbcType = jdbcType,
    encoder = encoder,
    decoder = decoder
) {
    lateinit var foreignTable: TableInfo
    override val inited get() = super.inited && this::foreignTable.isInitialized
}
