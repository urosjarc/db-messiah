package com.urosjarc.dbmessiah.domain.columns

import com.urosjarc.dbmessiah.domain.queries.QueryValue
import com.urosjarc.dbmessiah.domain.serialization.Decoder
import com.urosjarc.dbmessiah.domain.serialization.Encoder
import com.urosjarc.dbmessiah.domain.table.TableInfo
import com.urosjarc.dbmessiah.exceptions.ColumnException
import com.urosjarc.dbmessiah.extend.ext_kclass
import java.sql.JDBCType
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty1

abstract class Column(
    open val kprop: KProperty1<Any, Any?>,
    val dbType: String,
    val jdbcType: JDBCType,
    val encoder: Encoder<*>,
    val decoder: Decoder<*>,
) {
    lateinit var table: TableInfo
    val kclass get() = this.kprop.ext_kclass
    val name get() = this.table.escaper.wrap(this.kprop.name)
    val path: String get() = this.table.escaper.wrapJoin(this.table.schema, this.table.name, this.kprop.name)
    open val inited get() = this::table.isInitialized
    override fun equals(other: Any?): Boolean = this.hashCode() == other.hashCode()
    override fun hashCode(): Int = "$name${kclass.simpleName}".hashCode()

    override fun toString(): String {
        return "Column(name=${this.name}, dbType=${this.dbType}, jdbcType=${this.jdbcType.name}"
    }

    fun setValue(obj: Any, value: Any?) {
        try {
            val kp = this.kprop as KMutableProperty1<Any, Any?>
            try {
                kp.set(receiver = obj, value = value)
            } catch (e: ClassCastException) {
                throw ColumnException("Trying to set column $path value to '$value' but receiving object is missing matching property: $obj", e)
            }
        } catch (e: ClassCastException) {
            throw ColumnException("Trying to set column $path value to '$value' but the column is immutable!", e)
        }

    }

    fun getValue(obj: Any): Any? {
        try {
            return this.kprop.get(receiver = obj)
        } catch (e: Throwable) {
            throw ColumnException("Trying to get object value $path but failed, probably because property does not exists inside object: $obj", e)
        }

    }

    fun queryValue(obj: Any): QueryValue = QueryValue(name = this.kprop.name, value = this.getValue(obj = obj), jdbcType = this.jdbcType, encoder = this.encoder)
}
