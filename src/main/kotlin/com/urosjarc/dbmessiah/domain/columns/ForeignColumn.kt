package com.urosjarc.dbmessiah.domain.columns

import com.urosjarc.dbmessiah.domain.serialization.Decoder
import com.urosjarc.dbmessiah.domain.serialization.Encoder
import com.urosjarc.dbmessiah.domain.table.TableInfo
import java.awt.print.Book
import java.sql.JDBCType
import kotlin.reflect.KProperty1

class ForeignColumn(
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
