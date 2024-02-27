package com.urosjarc.dbmessiah.data

import java.sql.JDBCType
import kotlin.reflect.KProperty1

/**
 * Represents a column in a [TableInfo].
 *
 * @property kprop The reference to the class [KProperty1].
 * @property dbType The type of database value.
 * @property jdbcType The type of JDBC value.
 * @property encoder The [Encoder] used to encode the value.
 * @property decoder The [Decoder] used to decode the value.
 */
internal open class Column(
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
     * [TableInfo] instant who is parent or the owner of this [Column].
     */
    lateinit var table: TableInfo


    /**
     * Determines if [Column] has been initialized.
     */
    override val inited get() = this::table.isInitialized

    /**
     * Represents the name of a [Column].
     */
    override val name: String = this.kprop.name

    /**
     * Represents the full path of [Column] location.
     */
    override val path: String get() = listOf(this.table.schema, this.table.name, this.kprop.name).joinToString(".")

    /** @suppress */
    override fun toString(): String = "Column(name='${this.name}', dbType='${this.dbType}', jdbcType='${this.jdbcType.name}')"
}
