package com.urosjarc.dbjesus.extend

import com.urosjarc.dbjesus.domain.KClassProperty
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.KProperty1
import kotlin.reflect.full.declaredMemberProperties

val KProperty1<Any, *>.kclass get() = (this.returnType.classifier as KClass<Any>)
val KParameter.kclass get(): KClass<*> = this.type.classifier as KClass<*>

internal val KClass<out Any>.properties
    get(): List<KClassProperty> = this.declaredMemberProperties.map {
        KClassProperty(
            name = it.name,
            canBeNull = it.returnType.isMarkedNullable,
            kProperty1 = (it as KProperty1<Any, *>)
        )
    }
