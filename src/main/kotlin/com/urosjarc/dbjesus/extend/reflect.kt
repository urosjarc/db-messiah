package com.urosjarc.dbjesus.extend

import com.urosjarc.dbjesus.domain.KClassProperty
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.javaField

val Any.ext_kclass get(): KClass<*> = this::class
val KProperty1<*, *>.ext_kclass get() = this.returnType.classifier as KClass<*>
val KParameter.ext_kclass get(): KClass<*> = this.type.classifier as KClass<*>
val KClass<*>.ext_javaFields get() = this.memberProperties.filter { it.javaField != null } as Collection<KProperty1<Any, *>>

val KClass<*>.ext_properties
    get(): List<KClassProperty> = this.ext_javaFields.map {
        KClassProperty(
            name = it.name,
            canBeNull = it.returnType.isMarkedNullable,
            kProperty1 = it
        )
    }
