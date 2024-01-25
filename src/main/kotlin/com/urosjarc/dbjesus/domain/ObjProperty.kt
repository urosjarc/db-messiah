package com.urosjarc.dbjesus.domain

import com.urosjarc.dbjesus.extend.kclass
import kotlin.reflect.KProperty1


data class ObjProperty(
    val name: String,
    val value: Any?,
    val property: KProperty1<Any, *>,
    val serializer: DbTypeSerializer<Any>,
) {
    val encoder get() = this.serializer.encoder
    val kclass get() = this.property.kclass

    val jdbcType get() = this.serializer.jdbcType
}
