package com.urosjarc.dbmessiah.queries

import com.urosjarc.dbmessiah.Driver
import com.urosjarc.dbmessiah.Serializer
import com.urosjarc.dbmessiah.builders.ProcedureBuilder
import com.urosjarc.dbmessiah.builders.QueryBuilder
import com.urosjarc.dbmessiah.builders.SqlBuilder
import com.urosjarc.dbmessiah.data.Query
import com.urosjarc.dbmessiah.exceptions.QueryException
import kotlin.reflect.KClass

/**
 * A class used to execute multiple queries in one database call.
 * For executing only one `SELECT` query per call use [GetOneQueries].
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
     * @param buildSql the function that returns the SQL string for the query.
     * @return the object matrix, where each row represents list of objects from one query.
     */
    public fun get(vararg outputs: KClass<*>, buildSql: (SqlBuilder) -> String): List<List<Any>> {
        val query = this.ser.query(buildSql = buildSql)
        return this.executeQuery(query = query, outputs = outputs)
    }

    /**
     * Executes a custom SQL query with input and outputs.
     * Important is [buildSql] function which provides [ProcedureBuilder] to help you inject input value properties to returned SQL string.
     *
     * @param outputs the list of all tables from which rows will be fetched in the same order.
     * @param input The input object used to privide injected values to SQL statements.
     * @param buildSql The lambda function used to generate the SQL query string.
     * @return A list of lists containing the results of the query.
     */
    public fun <IN : Any> get(vararg outputs: KClass<*>, input: IN, buildSql: (queryBuilder: QueryBuilder<IN>) -> String): List<List<Any>> {
        val query = this.ser.queryWithInput(input = input, buildSql = buildSql)
        return this.executeQuery(query = query, outputs = outputs)
    }

    /**
     * Executes a database query with outputs using the provided SQL query object.
     * This is helper function to [get] functions since they have both same functionality.
     *
     * @param query The Query object representing the SQL query and its values.
     * @param outputs The list of all tables from which rows will be fetched in the same order.
     * @return The result of the query as a mutable list of lists, where each inner list represents a row of the result set.
     * @throws QueryException If the number of results does not match with the number of output classes.
     */
    private fun executeQuery(query: Query, vararg outputs: KClass<*>): MutableList<List<Any>> {
        val results = this.driver.execute(query = query) { i, rs ->
            this.ser.mapper.decodeMany(resultSet = rs, i = i, outputs = outputs)
        }

        if (results.size != outputs.size)
            throw QueryException("Number of results '${results.size}' does not match with number of output classes '${outputs.size}'")

        return results
    }
}
