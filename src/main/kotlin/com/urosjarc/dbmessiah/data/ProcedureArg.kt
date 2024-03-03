package com.urosjarc.dbmessiah.data

import java.sql.JDBCType
import kotlin.reflect.KProperty1

/**
 * Represents an argument for a [Procedure].
 */
public class ProcedureArg(
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

    /**
     * [Procedure] instant who is parent or the owner of this [ProcedureArg].
     */
    internal lateinit var procedure: Procedure

    override val inited: Boolean get() = this::procedure.isInitialized

    override val path: String get() = listOf(this.procedure.name, this.kprop.name).joinToString(".")

    /** @suppress */
    override fun toString(): String = "Arg(name='${this.name}', dbType='${this.dbType}', jdbcType='${this.jdbcType.name}')"
}
