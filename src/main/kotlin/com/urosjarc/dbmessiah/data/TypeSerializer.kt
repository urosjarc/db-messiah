package com.urosjarc.dbmessiah.data

import java.sql.JDBCType
import kotlin.reflect.KClass

/**
 * Represents a type serializer that is used to encode and decode value.
 *
 * @param T The type of value that the serializer handles.
 * @property kclass The [KClass] of the value type.
 * @property dbType The type of the value in the database.
 * @property jdbcType The JDBC data type of the value.
 * @property decoder The [Decoder] used to decode the value.
 * @property encoder The [Encoder] used to encode the value.
 */
public data class TypeSerializer<T : Any>(
    val kclass: KClass<T>,
    val dbType: String,
    val jdbcType: JDBCType,
    val decoder: Decoder<T?>, //At decoding you can accept null values
    val encoder: Encoder<T> //When converting to db value you must handle null values differently in JDBC!!!
) {
    /** @suppress */
    private val hash = kclass.hashCode()

    /** @suppress */
    override fun hashCode(): Int = this.hash

    /** @suppress */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as TypeSerializer<*>
        return kclass == other.kclass
    }


    /** @suppress */
    override fun toString(): String = "TS<${this.kclass.simpleName}>"
}
