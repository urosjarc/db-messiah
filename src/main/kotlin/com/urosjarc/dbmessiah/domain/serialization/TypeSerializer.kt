package com.urosjarc.dbmessiah.domain.serialization

import java.sql.JDBCType
import kotlin.reflect.KClass

class TypeSerializer<T : Any>(
    val kclass: KClass<T>,
    val dbType: String,
    val jdbcType: JDBCType,
    val decoder: Decoder<T?>, //At decoding you can accept null values
    val encoder: Encoder<T> //When converting to db value you must handle null values differently in JDBC!!!
) {
    override fun equals(other: Any?): Boolean = this.hashCode() == other.hashCode()
    override fun hashCode(): Int = kclass.hashCode()
    override fun toString(): String = "${this.kclass.simpleName}(kclass=${kclass.simpleName}, dbType=${dbType}, jdbcType=${jdbcType})"
}
