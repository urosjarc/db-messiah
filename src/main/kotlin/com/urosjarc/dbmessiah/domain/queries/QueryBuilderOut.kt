package com.urosjarc.dbmessiah.domain.queries

import com.urosjarc.dbmessiah.DbMessiahMapper
import com.urosjarc.dbmessiah.exceptions.SerializerException
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1


open class QueryBuilderOut<OUT: Any>(
    output: KClass<OUT>,
    mapper: DbMessiahMapper,
) : QueryBuilder(mapper = mapper) {
    init {
        if (!mapper.globalOutputs.contains(output))
            throw SerializerException("Output class '${output.simpleName}' is not registered in serializers global outputs!")
    }

    fun out(kp: KProperty1<OUT, *>): String = this.mapper.escaper.wrap(kp.name)

}
