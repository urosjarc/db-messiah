package com.urosjarc.dbmessiah.extend

import kotlin.reflect.*
import kotlin.reflect.full.primaryConstructor

private val KType.isWholeNumber: Boolean get() = listOf(typeOf<Int>(), typeOf<UInt>(), typeOf<Int?>(), typeOf<UInt?>()).contains(this)
public val <T : Any> KProperty1<T, *>.ext_canBeNull: Boolean get() = this.returnType.isMarkedNullable
public val <T : Any> KProperty1<T, *>.ext_isMutable: Boolean get() = this is KMutableProperty1<T, *>
public val <T : Any> KProperty1<T, *>.ext_isWholeNumber: Boolean get() = this.returnType.isWholeNumber
public val <T : Any> KProperty1<T, *>.ext_isInlineWholeNumber: Boolean
    get() {
        val kclass = this.returnType.classifier as KClass<*>
        if (kclass.isValue) {
            val firstParam = kclass.primaryConstructor?.parameters?.firstOrNull()
            return firstParam?.type?.isWholeNumber == true
        }
        return false
    }
