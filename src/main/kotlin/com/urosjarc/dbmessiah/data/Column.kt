package com.urosjarc.dbmessiah.data

import java.sql.JDBCType
import kotlin.reflect.KProperty1

/**
 * Represents a column in a database table.
 *
 * @property kprop The property reference to the column in the class.
 * @property dbType The type of the column in the database.
 * @property jdbcType The JDBC type of the column.
 * @property encoder The encoder used to encode the column value.
 * @property decoder The decoder used to decode the column value.
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
    override val name: String get() = this.kprop.name

    /**
     * Represents the full path of a [Column].
     */
    override val path: String get() = listOf(this.table.schema, this.table.name, this.kprop.name).joinToString(".")
    override fun toString(): String = "Column(name='${this.name}', dbType='${this.dbType}', jdbcType='${this.jdbcType.name}')"
}
