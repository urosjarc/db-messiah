package com.urosjarc.dbjesus.domain

import com.urosjarc.dbjesus.extend.ext_kclass
import kotlin.reflect.KProperty1


data class ObjProperty<T : Any>(
    val name: String,
    val value: T?,
    val property: KProperty1<Any, T?>,
    val serializer: TypeSerializer<T>,
) {
    val encoder get() = this.serializer.encoder
    val kclass get() = this.property.ext_kclass
    val jdbcType get() = this.serializer.jdbcType
}
