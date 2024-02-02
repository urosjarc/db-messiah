package com.urosjarc.dbmessiah.domain.columns

import com.urosjarc.dbmessiah.domain.serialization.DbValue
import com.urosjarc.dbmessiah.domain.serialization.Decoder
import com.urosjarc.dbmessiah.domain.serialization.Encoder
import com.urosjarc.dbmessiah.domain.table.TableInfo
import java.sql.JDBCType
import kotlin.reflect.KProperty1

open class Column(
    kprop: KProperty1<Any, Any?>,
    dbType: String,
    jdbcType: JDBCType,
    encoder: Encoder<*>,
    decoder: Decoder<*>,
) : DbValue(
    kprop = kprop,
    dbType = dbType,
    jdbcType = jdbcType,
    encoder = encoder,
    decoder = decoder
) {
    lateinit var table: TableInfo
    override val inited get() = this::table.isInitialized
    override val name: String get() = this.table.escaper.wrap(this.kprop.name)
    override val path: String get() = this.table.escaper.wrapJoin(this.table.schema, this.table.name, this.kprop.name)
    override fun toString(): String = "Column(name=${this.name}, dbType='${this.dbType}', jdbcType='${this.jdbcType.name}')"
}
