package com.urosjarc.dbmessiah.data

import java.sql.JDBCType
import kotlin.reflect.KProperty1

/**
 * Represents an argument for a [Procedure].
 *
 * @property kprop The Kotlin property used to access the argument value.
 * @property dbType The database type of the argument.
 * @property jdbcType The JDBC type of the argument.
 * @property encoder The [Encoder] used to encode the argument value.
 * @property decoder The [Decoder] used to decode the argument value.
 */
internal class ProcedureArg(
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
    /**
     * Represents the name of a [ProcedureArg].
     */
    override val name: String = kprop.name

    /**
     * [Procedure] instant who is parent or the owner of this [ProcedureArg].
     */
    lateinit var procedure: Procedure

    /**
     * Determines if [ProcedureArg] has been initialized.
     */
    override val inited: Boolean get() = this::procedure.isInitialized

    /**
     * Represents the full path of a [ProcedureArg] location.
     */
    override val path: String get() = listOf(this.procedure.name, this.kprop.name).joinToString(".")

    /** @suppress */
    override fun toString(): String = "Arg(name='${this.name}', dbType='${this.dbType}', jdbcType='${this.jdbcType.name}')"
}
