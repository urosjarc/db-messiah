package com.urosjarc.dbmessiah.domain.queries

import com.urosjarc.dbmessiah.Mapper
import com.urosjarc.dbmessiah.extend.ext_kclass
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

class QueryBuilderInOut<IN : Any, OUT: Any>(
    input: IN,
    output: KClass<OUT>,
    mapper: Mapper
) : QueryBuilderOut<IN, OUT>(input = input, mapper = mapper, output = output) {
    init {
        if (!mapper.globalInputs.contains(input::class))
            log.warn("input class '${input::class.simpleName}' is not registered in serializers global inputs!")
    }

    fun inp(kp: KProperty1<IN, *>): String {
        val ser = this.mapper.getGlobalSerializer(kclass = kp.ext_kclass)

        val qv = QueryValue(
            name = kp.name,
            jdbcType = ser.jdbcType,
            encoder = ser.encoder,
            value = kp.get(receiver = this.input)
        )

        this.queryValues.add(qv)

        return "?"
    }

}
