package com.urosjarc.dbjesus.domain.columns

import com.urosjarc.dbjesus.domain.serialization.Decoder
import com.urosjarc.dbjesus.domain.serialization.Encoder
import java.sql.JDBCType
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

interface Column {
    //Basic info
    val name: String
    val value: Any?

    //Constraints
    val unique: Boolean
    val notNull: Boolean

    //Type info
    val kprop: KProperty1<*, *>
    val kclass: KClass<*>
    val dbType: String
    val jdbcType: JDBCType

    //Serialization info
    val encoder: Encoder<*>
    val decoder: Decoder<*>
}
