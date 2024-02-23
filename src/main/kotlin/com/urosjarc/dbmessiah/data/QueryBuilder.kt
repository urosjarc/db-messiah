package com.urosjarc.dbmessiah.data

import com.urosjarc.dbmessiah.Mapper
import com.urosjarc.dbmessiah.exceptions.SerializerException
import kotlin.reflect.KProperty1


public open class QueryBuilder<IN : Any>(
    public val input: IN,
    private val mapper: Mapper
) {
    private val queryValues: MutableList<QueryValue> = mutableListOf()

    init {
        if (!mapper.globalInputs.contains(input::class))
            throw SerializerException("Input class '${input::class.simpleName}' is not registered in global inputs")
    }

    internal fun build(sql: String) = Query(sql = sql, values = this.queryValues.toTypedArray())
    public fun get(kp: KProperty1<IN, *>): String {
        val ser = this.mapper.getSerializer(kp)

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
