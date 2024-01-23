package com.urosjarc.dbjesus.domain

import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

class KClassField(
    val name: String,
    val canBeNull: Boolean,
    val kProperty1: KProperty1<Any, *>
) {
    val kclass get() = (this.kProperty1.returnType.classifier as KClass<Any>)
}
