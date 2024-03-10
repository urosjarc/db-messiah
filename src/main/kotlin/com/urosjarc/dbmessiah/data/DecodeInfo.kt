package com.urosjarc.dbmessiah.data

import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.KProperty1

/**
 * Represents the decoding information for a specific parameter in a Kotlin class.
 * Information is used in [Decoder] callback to help write custom [TypeSerializer].
 *
 * @param kclass The Kotlin class containing the property.
 * @param kparam The Kotlin constructor parameter representing the property (nullable if the property is not a constructor parameter).
 * @param kprop The Kotlin property representing the property (nullable if the property is not a member property).
 */
public data class DecodeInfo(
    public val kclass: KClass<*>,
    public val kparam: KParameter?,
    public val kprop: KProperty1<*, *>?,
)
