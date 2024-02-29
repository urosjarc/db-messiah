package com.urosjarc.dbmessiah.queries

import com.urosjarc.dbmessiah.Driver
import com.urosjarc.dbmessiah.Serializer
import com.urosjarc.dbmessiah.exceptions.DriverException
import com.urosjarc.dbmessiah.exceptions.MappingException
import kotlin.reflect.KClass

/**
 * Class responsible for executing database queries related to calling stored procedures.
 *
 * @param ser The serializer to be used for object serialization.
 * @param driver The database driver to be used for executing queries.
 */
public open class ProcedureQueries(
    private val ser: Serializer,
    private val driver: Driver
) {
    public fun <T : Any> drop(procedure: KClass<T>, throws: Boolean = true): Int {
        val query = this.ser.dropProcedure(procedure = procedure)
        try {
            return this.driver.update(query = query)
        } catch (e: DriverException) {
            if (throws) throw e
            return 0
        }
    }

    public open fun <T : Any> create(procedure: KClass<T>, throws: Boolean = true, body: () -> String): Int {
        val query = this.ser.createProcedure(procedure = procedure, sql = body())
        try {
            return this.driver.update(query = query)
        } catch (e: DriverException) {
            if (throws) throw e
            return 0
        }
    }

    public fun <T : Any> call(procedure: T, vararg outputs: KClass<*>): MutableList<List<Any>> {
        val query = this.ser.callProcedure(procedure = procedure)

        val results = this.driver.call(query = query) { i, rs ->
            this.ser.mapper.decodeMany(resultSet = rs, i = i, outputs = outputs)
        }

        if (results.size != outputs.size)
            throw MappingException("Number of results '${results.size}' does not match with number of output classes '${outputs.size}'")

        return results
    }

    public fun <T : Any, OUT : Any> call(procedure: T, output: KClass<OUT>): List<OUT> {
        val query = this.ser.callProcedure(procedure = procedure)

        return this.driver.call(query = query) { i, rs ->
            this.ser.mapper.decodeMany(resultSet = rs, i = i, outputs = arrayOf(output))
        }.firstOrNull() as List<OUT>
    }
}
