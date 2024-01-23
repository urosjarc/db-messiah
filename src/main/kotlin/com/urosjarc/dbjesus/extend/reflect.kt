package com.urosjarc.dbjesus.extend

import com.urosjarc.dbjesus.domain.KClassField
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.KProperty1
import kotlin.reflect.full.declaredMemberProperties

internal val KParameter.kclass get(): KClass<*> = this.type.classifier as KClass<*>

internal val KClass<out Any>.fields
    get(): List<KClassField> = this.declaredMemberProperties.map {
        KClassField(
            name = it.name,
            canBeNull = it.returnType.isMarkedNullable,
            kProperty1 = (it as KProperty1<Any, *>)
        )
    }
