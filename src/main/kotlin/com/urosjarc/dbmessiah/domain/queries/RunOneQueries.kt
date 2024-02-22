package com.urosjarc.dbmessiah.domain.queries

import com.urosjarc.dbmessiah.Driver
import com.urosjarc.dbmessiah.Serializer
import com.urosjarc.dbmessiah.domain.querie.QueryBuilder
import com.urosjarc.dbmessiah.exceptions.SerializerException
import kotlin.reflect.KClass

public class RunOneQueries(
    private val ser: Serializer,
    private val driver: Driver
) {

    public fun query(getSql: () -> String) {
        val query = this.ser.query(getSql = getSql)
        this.driver.execute(query = query) { i, rs ->
            this.ser.mapper.decodeMany(resultSet = rs, i = i)
        }
    }

    public fun <OUT : Any> query(output: KClass<OUT>, getSql: () -> String): List<OUT> {
        val query = this.ser.query(getSql = getSql)

        val results = this.driver.execute(query = query) { i, rs ->
            this.ser.mapper.decodeMany(resultSet = rs, i = i, output)
        }.firstOrNull() ?: throw SerializerException("Could not return first result from: $query")

        return results as List<OUT>
    }

    public fun <IN : Any, OUT : Any> query(output: KClass<OUT>, input: IN, getSql: (queryBuilder: QueryBuilder<IN>) -> String): List<OUT> {
        val query = this.ser.query(input = input, getSql = getSql)

        val results = this.driver.execute(query = query) { i, rs ->
            this.ser.mapper.decodeMany(resultSet = rs, i = i, output)
        }.firstOrNull() ?: throw SerializerException("Could not return first result from: $query")

        return results as List<OUT>
    }
}
