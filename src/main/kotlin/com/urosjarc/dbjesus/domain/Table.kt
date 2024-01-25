package com.urosjarc.dbjesus.domain

import com.urosjarc.dbjesus.exceptions.SerializerException
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.jvm.javaField

class Table(
    val primaryKey: KProperty1<*, *>,
    name: String? = null,
    val tableSerializers: List<TypeSerializer<Any>> = listOf()
) {
    val kClass: KClass<*> = (primaryKey.javaField?.declaringClass?.kotlin ?: throw SerializerException("Could not found enclosing class for primary key"))
    val name: String = name ?: kClass.simpleName.toString()
}
