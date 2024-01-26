package com.urosjarc.dbjesus.domain.queries

import com.urosjarc.dbjesus.Mapper
import com.urosjarc.dbjesus.domain.serialization.Encoder
import com.urosjarc.dbjesus.extend.ext_kclass
import java.sql.JDBCType
import kotlin.reflect.KProperty1

class QueryBuilder(val sourceObj: Any, val mapper: Mapper) {

    val encoders: MutableList<Encoder<*>> = mutableListOf()
    val values: MutableList<Any?> = mutableListOf()
    val jdbcType: MutableList<JDBCType> = mutableListOf()

    fun add(kp: KProperty1<*, *>) {

        val ser = this.mapper.getSerializer(
            tableKClass = sourceObj::class,
            propKClass = kp.ext_kclass
        )

        this.encoders.add(ser.encoder)
        this.values.add(jdbcType)
    }

    fun build(sql: String) = Query(sql = sql, encoders = this.encoders, values = this.values, jdbcTypes = this.jdbcType)
}