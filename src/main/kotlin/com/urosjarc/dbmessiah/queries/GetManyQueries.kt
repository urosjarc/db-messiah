package com.urosjarc.dbmessiah.queries

import com.urosjarc.dbmessiah.Driver
import com.urosjarc.dbmessiah.Serializer
import com.urosjarc.dbmessiah.data.Query
import com.urosjarc.dbmessiah.data.QueryBuilder
import com.urosjarc.dbmessiah.data.QueryEscaper
import com.urosjarc.dbmessiah.exceptions.MappingException
import kotlin.reflect.KClass

/**
 * A class used to execute multiple queries in one database call.
 * Note that some database supports multiple SQL statment per call.
 *
 * @property ser The serializer used to encode and decode objects.
 * @property driver The driver used to execute queries on the database.
 */
public class GetManyQueries(
    override val ser: Serializer,
    override val driver: Driver
) : GetOneQueries(ser = ser, driver = driver) {

    /**
     * Executes a database query with outputs using the provided SQL string.
     *
     * @param outputs the list of all tables from which rows will be fetched in the same order.
     * @param getSql the function that returns the SQL string for the query.
     * @return the object matrix, where each row represents list of objects from one query.
     * @throws MappingException if the number of database results does not match the number of output classes.
     */
    public fun get(vararg outputs: KClass<*>, getSql: (QueryEscaper) -> String): List<List<Any>> {
        val query = this.ser.query(getSql = getSql)
        return this.executeQuery(query = query, outputs = outputs)
    }

    /**
     * Executes a custom SQL query with input and outputs.
     * Important is [getSql] function which provides [QueryBuilder] to help you inject input value properties to returned SQL string.
     *
     * @param outputs the list of all tables from which rows will be fetched in the same order.
     * @param input The input object used to privide injected values to SQL statements.
     * @param getSql The lambda function used to generate the SQL query string.
     * @return A list of lists containing the results of the query.
     * @throws MappingException If the number of results does not match the number of output classes.
     */
    public fun <IN : Any> get(vararg outputs: KClass<*>, input: IN, getSql: (queryBuilder: QueryBuilder<IN>) -> String): List<List<Any>> {
        val query = this.ser.queryWithInput(input = input, getSql = getSql)
        return this.executeQuery(query = query, outputs = outputs)
    }

    private fun executeQuery(query: Query, vararg outputs: KClass<*>): MutableList<List<Any>> {
        val results = this.driver.execute(query = query) { i, rs ->
            this.ser.mapper.decodeMany(resultSet = rs, i = i, outputs = outputs)
        }

        if (results.size != outputs.size)
            throw MappingException("Number of results '${results.size}' does not match with number of output classes '${outputs.size}'")

        return results
    }
}
