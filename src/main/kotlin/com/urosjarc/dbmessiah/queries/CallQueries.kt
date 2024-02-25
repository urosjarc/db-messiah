package com.urosjarc.dbmessiah.queries

import com.urosjarc.dbmessiah.Driver
import com.urosjarc.dbmessiah.Serializer
import com.urosjarc.dbmessiah.exceptions.SerializerException
import kotlin.reflect.KClass

/**
 * Class responsible for executing database queries related to calling stored procedures.
 *
 * @param ser The serializer to be used for object serialization.
 * @param driver The database driver to be used for executing queries.
 */
public class CallQueries(
    private val ser: Serializer,
    private val driver: Driver
) {
    /**
     * Executes a stored procedure call.
     *
     * @param procedure The input object representing the stored procedure to be called.
     * @param outputs The output classes representing the result of the stored procedure call.
     * @return A list of lists representing the result of the stored procedure call.
     * @throws SerializerException if the number of results does not match the number of output classes.
     */
    public fun <IN : Any, OUT: Any> call(procedure: IN, vararg outputs: KClass<OUT>): List<List<Any>> {
        val query = this.ser.callQuery(obj = procedure)

        val results = this.driver.execute(query = query) { i, rs ->
            this.ser.mapper.decodeMany(resultSet = rs, i = i, outputs = outputs)
        }

        if (results.size != outputs.size)
            throw SerializerException("Number of results '${results.size}' does not match with number of output classes '${outputs.size}'")

        return results
    }
}
