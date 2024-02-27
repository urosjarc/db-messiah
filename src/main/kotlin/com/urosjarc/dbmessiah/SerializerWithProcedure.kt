package com.urosjarc.dbmessiah

import com.urosjarc.dbmessiah.data.Query
import com.urosjarc.dbmessiah.data.TypeSerializer
import kotlin.reflect.KClass

/**
 * SerializerWithProcedure is an internal interface that provides methods to create and call procedures.
 */
public abstract class SerializerWithProcedure(
    schemas: List<Schema>,
    globalSerializers: List<TypeSerializer<*>>,
    globalInputs: List<KClass<*>>,
    globalOutputs: List<KClass<*>>,
    globalProcedures: List<KClass<*>> = listOf()
) : Serializer(
    schemas = schemas,
    globalSerializers = globalSerializers,
    globalInputs = globalInputs,
    globalOutputs = globalOutputs,
    globalProcedures = globalProcedures
) {
    internal abstract fun <T : Any> createProcedure(procedure: KClass<T>, body: () -> String): Query
    internal abstract fun <T : Any> callProcedure(procedure: T): Query
}
