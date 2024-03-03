package com.urosjarc.dbmessiah.queries

import com.urosjarc.dbmessiah.Driver
import com.urosjarc.dbmessiah.Serializer
import com.urosjarc.dbmessiah.builders.ProcedureBuilder
import com.urosjarc.dbmessiah.exceptions.DriverException
import com.urosjarc.dbmessiah.exceptions.QueryException
import kotlin.reflect.KClass

/**
 * Class responsible for executing database queries related to calling stored procedures which
 * return selected rows.
 *
 * @param ser The serializer to be used for object serialization.
 * @param driver The database driver to be used for executing queries.
 */
public open class ProcedureQueries(
    public val ser: Serializer,
    public val driver: Driver
) {
    /**
     * Drops a stored procedure for the given type [T].
     *
     * @param throws Whether to throw any [DriverException] encountered during the procedure dropping or not.
     * @return The number of procedures dropped (0 if [throws] is false and a [DriverException] is encountered).
     */
    public inline fun <reified T : Any> drop(throws: Boolean = true): Int {
        val query = this.ser.dropProcedure(procedure = T::class)
        try {
            return this.driver.update(query = query)
        } catch (e: DriverException) {
            if (throws) throw e
            return 0
        }
    }

    /**
     * Creates a procedure of the specified type [T] and executes it.
     *
     * @param throws flag indicating whether or not to throw a [DriverException] if an error occurs during execution. Defaults to `true`.
     * @param body a lambda function that takes a [ProcedureBuilder] as parameter and returns a [String]. This lambda is used to build the procedure body.
     * @return the number of rows updated by the driver after executing the procedure.
     */
    public inline fun <reified T : Any> create(throws: Boolean = true, body: (ProcedureBuilder<T>) -> String): Int {
        val query = this.ser.createProcedure(procedure = T::class, procedureBody = body(ProcedureBuilder(ser = ser, procedure = T::class)))
        try {
            return this.driver.update(query = query)
        } catch (e: DriverException) {
            if (throws) throw e
            return 0
        }
    }

    /**
     * Executes a stored procedure [procedure] and returns the selected results.
     *
     * @param procedure The stored procedure to execute.
     * @param outputs The classes of the output parameters of the procedure.
     * @return The results of the stored procedure execution.
     */
    public fun <T : Any> call(procedure: T, vararg outputs: KClass<*>): MutableList<List<Any>> {
        val query = this.ser.callProcedure(procedure = procedure)

        val results = this.driver.call(query = query) { i, rs ->
            this.ser.mapper.decodeMany(resultSet = rs, i = i, outputs = outputs)
        }

        if (results.size != outputs.size)
            throw QueryException("Number of results '${results.size}' does not match with number of output classes '${outputs.size}'")

        return results
    }

    /**
     * Executes a stored procedure [procedure] and returns the selected results.
     *
     * @param procedure The stored procedure to execute.
     * @param output The class of the output parameter of the procedure.
     * @return The list of results of the stored procedure execution.
     */
    public fun <T : Any, OUT : Any> call(procedure: T, output: KClass<OUT>): List<OUT> {
        val query = this.ser.callProcedure(procedure = procedure)

        return this.driver.call(query = query) { i, rs ->
            this.ser.mapper.decodeMany(resultSet = rs, i = i, outputs = arrayOf(output))
        }.firstOrNull() as List<OUT>
    }
}
