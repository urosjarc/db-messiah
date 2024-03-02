package com.urosjarc.dbmessiah.extend

import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.jvm.javaField

internal val KClass<*>.ext_kparams: List<KParameter>? get() = this.primaryConstructor?.parameters?.filter { it.kind == KParameter.Kind.VALUE } // { INSTANCE, EXTENSION_RECEIVER, VALUE }
internal val KClass<*>.ext_kprops: List<KProperty1<out Any, *>> get() = this.memberProperties.filter { it.javaField != null }
