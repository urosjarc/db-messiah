package com.urosjarc.dbmessiah.domain.columns

import com.urosjarc.dbmessiah.domain.serialization.Decoder
import com.urosjarc.dbmessiah.domain.serialization.Encoder
import java.sql.JDBCType
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

interface Column {
    //Basic info
    val name: String

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

    fun value(obj: Any): Any? = (this.kprop as KProperty1<Any, *>).get(obj)
}
