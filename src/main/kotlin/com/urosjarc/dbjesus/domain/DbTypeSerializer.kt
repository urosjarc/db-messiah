package com.urosjarc.dbjesus.domain

import java.sql.JDBCType
import kotlin.reflect.KClass

class DbTypeSerializer<T : Any>(
    val kclass: KClass<T>,
    val dbType: String,
    val jdbcType: JDBCType,
    val decoder: Decoder<T>,
    val encoder: Encoder<T>
) {
    override fun equals(other: Any?): Boolean = this.hashCode() == other.hashCode()
    override fun hashCode(): Int = kclass.hashCode()
    override fun toString(): String = "${this.kclass.simpleName}(kclass=${kclass.simpleName}, dbType=${dbType}, jdbcType=${jdbcType})"
}
