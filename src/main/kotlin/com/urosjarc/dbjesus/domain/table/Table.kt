package com.urosjarc.dbjesus.domain.table

import com.urosjarc.dbjesus.domain.serialization.TypeSerializer
import com.urosjarc.dbjesus.exceptions.SerializerException
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.jvm.javaField

class Table(
    val primaryKey: KProperty1<*, *>,
    val foreignKeys: Map<KProperty1<*, *>, KClass<*>>,
    name: String? = null,
    val autoIncrement: Boolean = true,
    val tableSerializers: List<TypeSerializer<Any>> = listOf(),
) {
    val kClass: KClass<*> = (primaryKey.javaField?.declaringClass?.kotlin ?: throw SerializerException("Could not found enclosing class for primary key"))
    val name: String = name ?: kClass.simpleName.toString()
}
