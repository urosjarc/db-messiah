package com.urosjarc.dbmessiah.domain.call

import com.urosjarc.dbmessiah.domain.serialization.DbValue
import com.urosjarc.dbmessiah.domain.serialization.Decoder
import com.urosjarc.dbmessiah.domain.serialization.Encoder
import java.sql.JDBCType
import kotlin.reflect.KProperty1

class ProcedureArg(
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
    override val name: String = kprop.name
    lateinit var procedure: Procedure
    override val inited: Boolean get() = this::procedure.isInitialized
    override val path: String get() = listOf(this.procedure.name, this.kprop.name).joinToString(".")
    override fun toString(): String = "Arg(name=${this.name}, dbType='${this.dbType}', jdbcType='${this.jdbcType.name}')"
}
