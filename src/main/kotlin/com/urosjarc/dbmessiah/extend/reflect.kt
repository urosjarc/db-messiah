package com.urosjarc.dbmessiah.extend

import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.KProperty1
import kotlin.reflect.full.extensionReceiverParameter
import kotlin.reflect.full.instanceParameter
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.javaField

val KProperty1<*, *>.ext_receiver get() = (this.instanceParameter ?: this.extensionReceiverParameter)!!.ext_kclass
val KProperty1<*, *>.ext_kclass get() = this.returnType.classifier as KClass<*>
val KParameter.ext_kclass get(): KClass<*> = this.type.classifier as KClass<*>
val KClass<*>.ext_javaFields get() = this.memberProperties.filter { it.javaField != null }
