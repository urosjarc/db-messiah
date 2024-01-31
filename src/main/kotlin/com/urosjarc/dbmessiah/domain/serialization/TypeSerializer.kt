package com.urosjarc.dbmessiah.domain.serialization

import java.sql.JDBCType
import kotlin.reflect.KClass

data class TypeSerializer<T : Any>(
    val kclass: KClass<T>,
    val dbType: String,
    val jdbcType: JDBCType,
    val decoder: Decoder<T?>, //At decoding you can accept null values
    val encoder: Encoder<T> //When converting to db value you must handle null values differently in JDBC!!!
) {
    private val hash = kclass.hashCode()
    override fun equals(other: Any?): Boolean = this.hashCode() == other.hashCode()
    override fun hashCode(): Int = this.hash
    override fun toString(): String = "TS<${this.kclass.simpleName}>"
}
