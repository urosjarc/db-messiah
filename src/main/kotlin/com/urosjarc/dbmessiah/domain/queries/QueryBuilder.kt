package com.urosjarc.dbmessiah.domain.queries

import com.urosjarc.dbmessiah.DbMessiahMapper
import com.urosjarc.dbmessiah.exceptions.SerializerException
import kotlin.reflect.KProperty1


open class QueryBuilder<IN : Any>(
    val input: IN,
    val mapper: DbMessiahMapper
) {
    val queryValues: MutableList<QueryValue> = mutableListOf()
    fun build(sql: String) = Query(sql = sql, values = this.queryValues.toTypedArray())
    init {
        if (!mapper.globalInputs.contains(input::class))
            throw SerializerException("input class '${input::class.simpleName}' is not registered in serializers global inputs!")
    }
    fun get(kp: KProperty1<IN, *>): String {
        val ser = this.mapper.getSerializer(kp)

        val qv = QueryValue(
            name = kp.name,
            jdbcType = ser.jdbcType,
            encoder = ser.encoder,
            value = if(this.input != null) kp.get(receiver = this.input) else null
        )

        this.queryValues.add(qv)

        return "?"
    }

}
