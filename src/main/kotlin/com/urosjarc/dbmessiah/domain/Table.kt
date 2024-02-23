package com.urosjarc.dbmessiah.domain

import com.urosjarc.dbmessiah.data.TypeSerializer
import com.urosjarc.dbmessiah.exceptions.SerializerException
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty1
import kotlin.reflect.full.extensionReceiverParameter
import kotlin.reflect.full.instanceParameter
import kotlin.reflect.jvm.javaField

public class Table<T : Any>(
    public val primaryKey: KMutableProperty1<out T, out Any?>,
    public val foreignKeys: List<Pair<KProperty1<T, *>, KClass<*>>> = listOf(),
    internal var constraints: List<Pair<KProperty1<T, *>, List<C>>> = listOf(),
    internal val serializers: List<TypeSerializer<*>> = listOf(),
    internal val columnSerializers: List<Pair<KProperty1<T, *>, TypeSerializer<Any>>> = listOf()
) {
    init {
        //If user has not provided constraints for primary key, then add default ones here
        this.primaryKey as KProperty1<T, *>
        if (!this.constraints.map { it.first }.contains(this.primaryKey)) {
            val constrs = this.constraints.toMutableList()
            constrs.add(Pair(first = this.primaryKey, second = listOf(C.AUTO_INC)))
            this.constraints = constrs
        }
    }

    public val name: String = ((primaryKey.instanceParameter ?: primaryKey.extensionReceiverParameter)!!.type.classifier as KClass<*>).simpleName.toString()

    public val kclass: KClass<*> = (primaryKey.javaField?.declaringClass?.kotlin ?: throw SerializerException("Could not found enclosing class for primary key"))
    internal val primaryKeyConstraints get() = this.constraintsFor(kprop = this.primaryKey)

    private val hash = this.name.hashCode()
    override fun hashCode(): Int = this.hash

    override fun equals(other: Any?): Boolean {
        return this.hashCode() == other.hashCode()
    }
    override fun toString(): String = "'${this.name}'"

    internal fun constraintsFor(kprop: KProperty1<*, *>) = this.constraints.firstOrNull { it.first == kprop }?.second ?: listOf()
}
