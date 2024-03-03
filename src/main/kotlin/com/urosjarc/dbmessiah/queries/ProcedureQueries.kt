package com.urosjarc.dbmessiah.queries

import com.urosjarc.dbmessiah.Driver
import com.urosjarc.dbmessiah.Serializer
import com.urosjarc.dbmessiah.builders.ProcedureBuilder
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
    public val ser: Serializer,
    public val driver: Driver
) {
    public inline fun <reified T : Any> drop(throws: Boolean = true): Int {
        val query = this.ser.dropProcedure(procedure = T::class)
        try {
            return this.driver.update(query = query)
        } catch (e: DriverException) {
            if (throws) throw e
            return 0
        }
    }

    public inline fun <reified T : Any> create(throws: Boolean = true, body: (ProcedureBuilder<T>) -> String): Int {
        val query = this.ser.createProcedure(procedure = T::class, procedureBody = body(ProcedureBuilder(ser = ser, procedure = T::class)))
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
