package com.urosjarc.dbmessiah.domain.columns

import com.urosjarc.dbmessiah.domain.serialization.Decoder
import com.urosjarc.dbmessiah.domain.serialization.Encoder
import com.urosjarc.dbmessiah.extend.ext_kclass
import java.sql.JDBCType
import kotlin.reflect.KProperty1

abstract class Column(
    val kprop: KProperty1<*, *>,
    val dbType: String,
    val jdbcType: JDBCType,
    val encoder: Encoder<*>,
    val decoder: Decoder<*>
) {

    val kclass get() = this.kprop.ext_kclass
    val name get() = this.kprop.name

    fun value(obj: Any): Any? = (this.kprop as KProperty1<Any, *>).get(obj)
    override fun equals(other: Any?): Boolean = this.hashCode() == other.hashCode()
    override fun hashCode(): Int = "$name${kclass.simpleName}".hashCode()

}
