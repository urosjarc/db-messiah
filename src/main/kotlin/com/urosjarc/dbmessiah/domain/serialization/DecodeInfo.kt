package com.urosjarc.dbmessiah.domain.serialization

import kotlin.reflect.KClass
import kotlin.reflect.KParameter

class DecodeInfo(
    val kclass: KClass<*>,
    val kparam: KParameter,
)
