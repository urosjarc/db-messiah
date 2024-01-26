package com.urosjarc.dbjesus.domain.table

import com.urosjarc.dbjesus.domain.columns.C
import com.urosjarc.dbjesus.domain.serialization.TypeSerializer
import com.urosjarc.dbjesus.exceptions.SerializerException
import com.urosjarc.dbjesus.extend.ext_kclass
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.jvm.javaField

class Table<T : Any>(
    val primaryKey: KProperty1<T, *>,
    val foreignKeys: List<Pair<KProperty1<T, *>, KClass<*>>> = listOf(),
    val constraints: List<Pair<KProperty1<T, *>, List<C>>> = listOf(),
    val serializers: List<TypeSerializer<Any>> = listOf(),
) {
    val name: String = primaryKey.ext_kclass.simpleName.toString()
    val kclass: KClass<*> = (primaryKey.javaField?.declaringClass?.kotlin ?: throw SerializerException("Could not found enclosing class for primary key"))
    val primaryKeyConstraints get() = this.constraintsFor(kprop = this.primaryKey)
    fun constraintsFor(kprop: KProperty1<*, *>) = this.constraints.firstOrNull { it.first == kprop }?.second ?: listOf()
}
