package com.urosjarc.dbmessiah.data

import java.sql.JDBCType
import kotlin.reflect.KProperty1

/**
 * Represents a column in a database table.
 */
public open class Column(
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
    public lateinit var table: TableInfo

    override val inited: Boolean get() = this::table.isInitialized

    override val name: String = this.kprop.name

    override val path: String get() = listOf(this.table.schema, this.table.name, this.kprop.name).joinToString(".")

    /** @suppress */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as Column

        return path == other.path
    }

    /** @suppress */
    override fun toString(): String = "Column(name='${this.name}', dbType='${this.dbType}', jdbcType='${this.jdbcType.name}')"


}
