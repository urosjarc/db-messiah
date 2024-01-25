package com.urosjarc.dbjesus.domain

import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

class KClassProperty(
    val name: String,
    val canBeNull: Boolean,
    val kProperty1: KProperty1<*, *>
) {
    val kclass get() = this.kProperty1.returnType.classifier as KClass<*>
}
