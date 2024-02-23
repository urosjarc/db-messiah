package com.urosjarc.dbmessiah.data

import kotlin.reflect.KClass
import kotlin.reflect.KParameter

/**
 * Represents the decoding information for a specific parameter in a Kotlin class.
 * Information is used in [Decoder] callback to help write custom [TypeSerializer].
 *
 * @property kclass The [KClass] that contains the [kparam].
 * @property kparam The parameter which holds value to be decoded.
 */
public class DecodeInfo(
    internal val kclass: KClass<*>,
    internal val kparam: KParameter,
)
