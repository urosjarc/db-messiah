package com.urosjarc.dbjesus.domain.table

import com.urosjarc.dbjesus.domain.columns.C
import com.urosjarc.dbjesus.domain.serialization.TypeSerializer
import com.urosjarc.dbjesus.exceptions.SerializerException
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.jvm.javaField

class Table<T : Any>(
    val primaryKey: KProperty1<T, *>,
    val foreignKeys: List<Pair<KProperty1<T, *>, KClass<*>>>,
    name: String? = null,
    val constraints: List<Pair<KProperty1<T, *>, List<C>>> = listOf(),
    val tableSerializers: List<TypeSerializer<Any>> = listOf(),
) {
    val kclass: KClass<*> = (primaryKey.javaField?.declaringClass?.kotlin ?: throw SerializerException("Could not found enclosing class for primary key"))
    val name: String = name ?: kclass.simpleName.toString()
    val primaryKeyConstraints get() = this.constraintsFor(kprop = this.primaryKey)
    fun constraintsFor(kprop: KProperty1<*, *>) = this.constraints.firstOrNull { it.first == kprop }?.second ?: listOf()
}
