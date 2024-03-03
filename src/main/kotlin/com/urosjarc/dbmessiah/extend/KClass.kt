package com.urosjarc.dbmessiah.extend

import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.jvm.javaField

/**
 * Retrieves the list of constructor parameters [KParameter] for the given class [KClass].
 *
 * @return The list of constructor parameters for the given class, or null if the class does not have a primary constructor.
 */
internal val KClass<*>.ext_kparams: List<KParameter>? get() = this.primaryConstructor?.parameters?.filter { it.kind == KParameter.Kind.VALUE } // { INSTANCE, EXTENSION_RECEIVER, VALUE }

/**
 * Retrieves the list of properties [KProperty1] for a given class [KClass].
 *
 * @param kclass The class for which to retrieve the properties.
 * @return The list of properties for the given class.
 */
internal val KClass<*>.ext_kprops: List<KProperty1<out Any, *>> get() = this.memberProperties.filter { it.javaField != null }
