package com.urosjarc.dbmessiah.domain.queries

import com.urosjarc.dbmessiah.Mapper
import com.urosjarc.dbmessiah.extend.ext_kclass
import kotlin.reflect.KProperty1

class QueryBuilder<T: Any>(val sourceObj: T, val mapper: Mapper) {

    val queryValues: MutableList<QueryValue> = mutableListOf()

    fun add(kp: KProperty1<T, *>) {
        val ser = this.mapper.getSerializer(
            tableKClass = sourceObj::class,
            propKClass = kp.ext_kclass
        )

        val qv = QueryValue(
            name = kp.name,
            jdbcType = ser.jdbcType,
            encoder = ser.encoder,
            value = kp.get(receiver = sourceObj)
        )

        this.queryValues.add(qv)
    }

    fun build(sql: String) = Query(sql = sql, values = queryValues.toTypedArray())
}
