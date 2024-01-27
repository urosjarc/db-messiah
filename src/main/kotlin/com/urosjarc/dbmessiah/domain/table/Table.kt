package com.urosjarc.dbmessiah.domain.table

import com.urosjarc.dbmessiah.domain.columns.C
import com.urosjarc.dbmessiah.domain.serialization.TypeSerializer
import com.urosjarc.dbmessiah.exceptions.SerializerException
import com.urosjarc.dbmessiah.extend.ext_kclass
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.jvm.javaField

class Table<T : Any>(
    val primaryKey: KProperty1<T, *>,
    val foreignKeys: List<Pair<KProperty1<T, *>, KClass<*>>> = listOf(),
    var constraints: List<Pair<KProperty1<T, *>, List<C>>> = listOf(),
    val serializers: List<TypeSerializer<Any>> = listOf(),
    val columnSerializers: List<Pair<KProperty1<T, *>, TypeSerializer<Any>>> = listOf()
) {
    init {
        //If user has not provided constraints for primary key, then add default ones here
        if (!this.constraints.map { it.first }.contains(this.primaryKey)) {
            val constrs = this.constraints.toMutableList()
            constrs.add(Pair(first = this.primaryKey, second = listOf(C.AUTO_INC)))
            this.constraints = constrs
        }
    }

    val name: String = primaryKey.ext_kclass.simpleName.toString()
    val kclass: KClass<*> = (primaryKey.javaField?.declaringClass?.kotlin ?: throw SerializerException("Could not found enclosing class for primary key"))
    val primaryKeyConstraints get() = this.constraintsFor(kprop = this.primaryKey)
    fun constraintsFor(kprop: KProperty1<*, *>) = this.constraints.firstOrNull { it.first == kprop }?.second ?: listOf()
}
