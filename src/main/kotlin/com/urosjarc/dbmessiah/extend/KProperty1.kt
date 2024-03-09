package com.urosjarc.dbmessiah.extend

import java.util.*
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
 * Checks if the given type is a UUID.
 *
 * @return true if the type is a UUID, false otherwise.
 */
private val KType.ext_isUUID: Boolean get() = listOf(typeOf<UUID>()).contains(this)

/**
 * The [ext_owner] property is an extension property for the [KProperty1] class.
 * It returns the class that owns the property.
 *
 * @return The [KClass] object representing the owning class of the property.
 */
public val <T : Any> KProperty1<T, *>.ext_owner: KClass<T> get() = this.javaField?.declaringClass?.kotlin as KClass<T>

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
 * Determines whether the property represents a UUID.
 *
 * @return true if the property is of type UUID, otherwise false.
 */
public val <T : Any> KProperty1<T, *>.ext_isUUID: Boolean get() = this.returnType.ext_isUUID


/**
 * Checks if the property is an immutable, non-optional inline whole number.
 *
 * @receiver The `KProperty1` object.
 * @return `true` if the property is an immutable, non-optional inline whole number, `false` otherwise.
 */
public val <T : Any> KProperty1<T, *>.ext_isInlineWholeNumber: Boolean
    get() {
        val kclass = this.returnType.classifier as KClass<*>
        if (kclass.isValue) {
            val firstParam = kclass.primaryConstructor?.parameters?.firstOrNull()
            return firstParam?.type?.ext_isWholeNumber == true && !firstParam.isOptional && !firstParam.type.isMarkedNullable
        }
        return false
    }

/**
 * Checks if the property is an immutable, non-optional inline UUID.
 *
 * @receiver The `KProperty1` object.
 * @return `true` if the property is an immutable, non-optional inline UUID, `false` otherwise.
 */
public val <T : Any> KProperty1<T, *>.ext_isInlineUUID: Boolean
    get() {
        val kclass = this.returnType.classifier as KClass<*>
        if (kclass.isValue) {
            val firstParam = kclass.primaryConstructor?.parameters?.firstOrNull()
            return firstParam?.type?.ext_isUUID == true && !firstParam.isOptional && !firstParam.type.isMarkedNullable
        }
        return false
    }

/**
 * This property represents whether the given property is an property representing auto-generated UUID.
 * It returns `true` if the property meets the following criteria:
 * - The property is either an inline UUID or a regular UUID
 * - The property is not mutable
 * - The property's return type is marked as nullable
 *
 * @return The flag indicating whether the property is an auto-generated UUID.
 */
public val <T : Any> KProperty1<T, *>.ext_isAutoUUID: Boolean
    get() = !listOf(
        (this.ext_isInlineUUID || this.ext_isUUID),
        this.ext_isMutable, this.returnType.isMarkedNullable
    ).contains(false)

/**
 * Indicates whether the property is representing auto-incrementing property.
 * It returns `true` if the property meets the following criteria:
 * - The property is either an inline or a regular whole number
 * - The property is not mutable
 * - The property's return type is marked as nullable
 *
 * @return true if the property is an auto-incrementing property, false otherwise.
 */
public val <T : Any> KProperty1<T, *>.ext_isAutoInc: Boolean
    get() = !listOf(
        (this.ext_isInlineWholeNumber || this.ext_isWholeNumber),
        this.ext_isMutable, this.returnType.isMarkedNullable
    ).contains(false)
