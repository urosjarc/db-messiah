package com.urosjarc.dbmessiah.queries

import com.urosjarc.dbmessiah.Driver
import com.urosjarc.dbmessiah.Serializer
import com.urosjarc.dbmessiah.data.QueryBuilder
import com.urosjarc.dbmessiah.exceptions.SerializerException
import kotlin.reflect.KClass

/**
 * A class used to execute multiple queries in one database call.
 * Note that some database supports multiple SQL statment per call.
 *
 * @property ser The serializer used to encode and decode objects.
 * @property driver The driver used to execute queries on the database.
 */
public class RunManyQueries(
    private val ser: Serializer,
    private val driver: Driver
) {

    /**
     * Executes a query without input or outputs using the provided SQL statement.
     *
     * @param getSql A function that returns the user provided SQL statement to be executed.
     */
    public fun query(getSql: () -> String) {
        val query = this.ser.query(getSql = getSql)
        this.driver.execute(query = query) { i, rs ->
            this.ser.mapper.decodeMany(resultSet = rs, i = i)
        }
    }

    /**
     * Executes a database query with outputs using the provided SQL string.
     *
     * @param outputs the list of all tables from which rows will be fetched in the same order.
     * @param getSql the function that returns the SQL string for the query.
     * @return the object matrix, where each row represents list of objects from one query.
     * @throws SerializerException if the number of database results does not match the number of output classes.
     */
    public fun query(vararg outputs: KClass<*>, getSql: () -> String): List<List<Any>> {
        val query = this.ser.query(getSql = getSql)

        val results = this.driver.execute(query = query) { i, rs ->
            this.ser.mapper.decodeMany(resultSet = rs, i = i, outputs = outputs)
        }

        if (results.size != outputs.size)
            throw SerializerException("Number of results '${results.size}' does not match with number of output classes '${outputs.size}'")

        return results
    }

    /**
     * Executes a custom SQL query with input and outputs.
     * Important is [getSql] function which provides [QueryBuilder] to help you inject input value properties to returned SQL string.
     *
     * @param outputs the list of all tables from which rows will be fetched in the same order.
     * @param input The input object used to privide injected values to SQL statements.
     * @param getSql The lambda function used to generate the SQL query string.
     * @return A list of lists containing the results of the query.
     * @throws SerializerException If the number of results does not match the number of output classes.
     */
    public fun <IN : Any> query(vararg outputs: KClass<*>, input: IN, getSql: (queryBuilder: QueryBuilder<IN>) -> String): List<List<Any>> {
        val query = this.ser.query(input = input, getSql = getSql)

        val results = this.driver.execute(query = query) { i, rs ->
            this.ser.mapper.decodeMany(resultSet = rs, i = i, outputs = outputs)
        }

        if (results.size != outputs.size)
            throw SerializerException("Number of results '${results.size}' does not match with number of output classes '${outputs.size}'")

        return results
    }
}
