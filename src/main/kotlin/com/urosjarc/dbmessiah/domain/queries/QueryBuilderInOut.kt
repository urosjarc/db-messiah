package com.urosjarc.dbmessiah.domain.queries

import com.urosjarc.dbmessiah.DbMessiahRepository
import com.urosjarc.dbmessiah.exceptions.SerializerException
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

class QueryBuilderInOut<IN : Any, OUT : Any>(
    val input: IN,
    output: KClass<OUT>,
    repo: DbMessiahRepository
) : QueryBuilderOut<OUT>(repo = repo, output = output) {
    init {
        if (!repo.globalInputs.contains(input::class))
            throw SerializerException("input class '${input::class.simpleName}' is not registered in serializers global inputs!")
    }

    fun inp(kp: KProperty1<IN, *>): String {
        val ser = this.repo.getSerializer(kp)

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
