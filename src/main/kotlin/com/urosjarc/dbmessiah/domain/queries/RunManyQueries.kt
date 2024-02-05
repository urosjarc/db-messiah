package com.urosjarc.dbmessiah.domain.queries

import com.urosjarc.dbmessiah.Driver
import com.urosjarc.dbmessiah.Serializer
import com.urosjarc.dbmessiah.domain.querie.QueryBuilder
import com.urosjarc.dbmessiah.exceptions.SerializerException
import kotlin.reflect.KClass

class RunManyQueries(val ser: Serializer, val driver: Driver) {
    fun query(getSql: () -> String) {
        val query = this.ser.query(getSql = getSql)
        this.driver.execute(query = query) { i, rs ->
            this.ser.mapper.decodeMany(resultSet = rs, i = i)
        }
    }

    fun query(vararg outputs: KClass<*>, getSql: () -> String): List<List<Any>> {
        val query = this.ser.query(getSql = getSql)

        val results = this.driver.execute(query = query) { i, rs ->
            this.ser.mapper.decodeMany(resultSet = rs, i = i, outputs = outputs)
        }

        if (results.size != outputs.size)
            throw SerializerException("Number of results '${results.size}' does not match with number of output classes '${outputs.size}'")

        return results
    }

    fun <IN : Any> query(vararg outputs: KClass<*>, input: IN, getSql: (queryBuilder: QueryBuilder<IN>) -> String): List<List<Any>> {
        val query = this.ser.query(input = input, getSql = getSql)

        val results = this.driver.execute(query = query) { i, rs ->
            this.ser.mapper.decodeMany(resultSet = rs, i = i, outputs = outputs)
        }

        if (results.size != outputs.size)
            throw SerializerException("Number of results '${results.size}' does not match with number of output classes '${outputs.size}'")

        return results
    }
}
