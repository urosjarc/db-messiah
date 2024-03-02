package com.urosjarc.dbmessiah.queries

import com.urosjarc.dbmessiah.Driver
import com.urosjarc.dbmessiah.Serializer
import com.urosjarc.dbmessiah.data.QueryBuilder
import com.urosjarc.dbmessiah.data.QueryEscaper
import com.urosjarc.dbmessiah.exceptions.MappingException
import kotlin.reflect.KClass

/**
 * Class used to execute single query in one database call.
 * Note that some database supports only one SQL statment per call.
 *
 * @param ser The serializer used to map objects to SQL queries.
 * @param driver The driver used to execute the SQL queries.
 */
public open class RunOneQueries(
    public open val ser: Serializer,
    public open val driver: Driver
) {

    /**
     * Executes a query without input or output using the provided SQL statement.
     *
     * @param getSql A function that returns the user provided SQL statement to be executed.
     */
    public fun query(getSql: (QueryEscaper) -> String) {
        val query = this.ser.query(getSql = getSql)
        this.driver.execute(query = query) { i, rs ->
            this.ser.mapper.decodeMany(resultSet = rs, i = i)
        }
    }

    /**
     * Executes a custom SQL query with output using the provided SQL string.
     *
     * @param output the table from which rows will be fetched.
     * @param getSql the function that returns the SQL string for the query.
     * @return fetched rows from [output] table.
     * @throws MappingException if the number of database results does not match the number of output classes.
     */
    public inline fun <reified OUT : Any> query(noinline getSql: (QueryEscaper) -> String): List<OUT> {
        val query = this.ser.query(getSql = getSql)
        return this.driver.query(query = query) {
            this.ser.mapper.decodeOne(resultSet = it, kclass = OUT::class)
        }
    }

    /**
     * Executes a custom SQL query with input and output.
     *
     * @param output the table from which rows will be fetched.
     * @param input The input object used to privide injected values to SQL statements.
     * @param getSql A function that takes a query builder and returns the SQL query as a string.
     * @return A list of query results of type [OUT].
     * @throws MappingException if the query does not return any results.
     */
    public inline fun <IN : Any, reified OUT : Any> query(input: IN, noinline getSql: (queryBuilder: QueryBuilder<IN>) -> String): List<OUT> {
        val query = this.ser.queryWithInput(input = input, getSql = getSql)
        return this.driver.query(query = query) {
            this.ser.mapper.decodeOne(resultSet = it, kclass = OUT::class)
        }
    }
}
