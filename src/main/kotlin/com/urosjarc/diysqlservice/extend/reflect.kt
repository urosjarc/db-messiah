package com.urosjarc.diysqlservice.extend

import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.KProperty1

val <T> KProperty1<T, *>.canBeNull get(): Boolean = this.returnType.isMarkedNullable
val <T> KProperty1<T, *>.kclass get(): KClass<*> = this.returnType.classifier as KClass<*>

val KParameter.kclass get(): KClass<*> = this.type.classifier as KClass<*>
