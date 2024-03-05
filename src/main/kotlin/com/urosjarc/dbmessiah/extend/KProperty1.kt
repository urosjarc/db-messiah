package com.urosjarc.dbmessiah.extend

import kotlin.reflect.*
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.jvm.javaField

/**
 * Determines if the given [KType] is a whole number type.
 *
 * @return True if the type is a whole number type, false otherwise.
 */
private val KType.ext_isWholeNumber: Boolean get() = listOf(typeOf<Int>(), typeOf<UInt>(), typeOf<Int?>(), typeOf<UInt?>()).contains(this)

/**
 * The [ext_owner] property is an extension property for the [KProperty1] class.
 * It returns the class that owns the property.
 *
 * @return The [KClass] object representing the owning class of the property.
 */
public val <T : Any> KProperty1<T, *>.ext_owner: KClass<T>? get() = this.javaField?.declaringClass?.kotlin as KClass<T>?

/**
 * Determines whether the property is mutable or not.
 *
 * @return true if the property is mutable, false otherwise.
 */
public val <T : Any> KProperty1<T, *>.ext_isMutable: Boolean get() = this is KMutableProperty1<T, *>

/**
 * Indicates whether the property represents a whole number.
 *
 * @return true if the property represents a whole number, false otherwise.
 */
public val <T : Any> KProperty1<T, *>.ext_isWholeNumber: Boolean get() = this.returnType.ext_isWholeNumber

/**
 * Represents whether the [KProperty1] is whole number.
 *
 * @return `true` if the [KProperty1] is an inline whole number, `false` otherwise.
 */
public val <T : Any> KProperty1<T, *>.ext_isInlineWholeNumber: Boolean
    get() {
        val kclass = this.returnType.classifier as KClass<*>
        if (kclass.isValue) {
            val firstParam = kclass.primaryConstructor?.parameters?.firstOrNull()
            return firstParam?.type?.ext_isWholeNumber == true
        }
        return false
    }
