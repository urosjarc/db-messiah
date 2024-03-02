package com.urosjarc.dbmessiah.extend

import kotlin.reflect.*
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.jvm.javaField

private val KType.ext_isWholeNumber: Boolean get() = listOf(typeOf<Int>(), typeOf<UInt>(), typeOf<Int?>(), typeOf<UInt?>()).contains(this)
public val <T : Any> KProperty1<T, *>.ext_owner: KClass<T> get() = this.javaField?.declaringClass?.kotlin as KClass<T>
public val <T : Any> KProperty1<T, *>.ext_isMutable: Boolean get() = this is KMutableProperty1<T, *>
public val <T : Any> KProperty1<T, *>.ext_isWholeNumber: Boolean get() = this.returnType.ext_isWholeNumber
public val <T : Any> KProperty1<T, *>.ext_isInlineWholeNumber: Boolean
    get() {
        val kclass = this.returnType.classifier as KClass<*>
        if (kclass.isValue) {
            val firstParam = kclass.primaryConstructor?.parameters?.firstOrNull()
            return firstParam?.type?.ext_isWholeNumber == true
        }
        return false
    }
