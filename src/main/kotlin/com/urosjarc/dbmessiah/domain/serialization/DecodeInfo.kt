package com.urosjarc.dbmessiah.domain.serialization

import kotlin.reflect.KClass
import kotlin.reflect.KParameter

public class DecodeInfo(
    internal val kclass: KClass<*>,
    internal val kparam: KParameter,
)
