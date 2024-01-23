package com.urosjarc.dbjesus.domain

import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

class ObjField(
    val name: String,
    val encodedValue: String,
    val kProperty1: KProperty1<Any, *>
) {
    val kclass get() = (this.kProperty1.returnType.classifier as KClass<Any>)
}
