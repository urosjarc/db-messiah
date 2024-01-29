package com.urosjarc.dbmessiah.domain.queries

import com.urosjarc.dbmessiah.Mapper
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1


open class QueryBuilderOut<IN: Any, OUT: Any>(
    input: IN,
    output: KClass<OUT>,
    mapper: Mapper,
) : QueryBuilder<IN>(input = input, mapper = mapper) {
    init {
        if (!mapper.globalOutputs.contains(output))
            log.warn("Output class '${output.simpleName}' is not registered in serializers global outputs!")
    }

    fun out(kp: KProperty1<OUT, *>): String = this.mapper.escaper.wrap(kp.name)

}
