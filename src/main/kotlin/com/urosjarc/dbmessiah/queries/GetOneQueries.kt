package com.urosjarc.dbmessiah.queries

import com.urosjarc.dbmessiah.Driver
import com.urosjarc.dbmessiah.Serializer
import com.urosjarc.dbmessiah.builders.QueryBuilder
import com.urosjarc.dbmessiah.builders.SqlBuilder
import com.urosjarc.dbmessiah.exceptions.MappingException
import kotlin.reflect.KClass

/**
 * Class used to execute single query in one database call.
 * For executing multiple `SELECT` queries per call use [GetManyQueries].
 *
 * @param ser The serializer used to map objects to SQL queries.
 * @param driver The driver used to execute the SQL queries.
 */
public open class GetOneQueries(
    public open val ser: Serializer,
    public open val driver: Driver
) {

    /**
     * Executes a query without input or output using the provided SQL statement.
     *
     * @param buildSql A function that returns the user provided SQL statement to be executed.
     */
    public fun run(buildSql: (SqlBuilder) -> String) {
        val query = this.ser.query(buildSql = buildSql)
        this.driver.execute(query = query) { i, rs ->
            this.ser.mapper.decodeMany(resultSet = rs, i = i)
        }
    }

    /**
     * Executes a custom SQL query with output using the provided SQL string.
     *
     * @param buildSql A function that takes a [SqlBuilder] and returns the user provided SQL statement to be executed.
     * @return A [List] of objects of type [OUT], obtained by decoding each row of the result set.
     */
    public inline fun <reified OUT : Any> get(noinline buildSql: (SqlBuilder) -> String): List<OUT> {
        val query = this.ser.query(buildSql = buildSql)
        return this.driver.query(query = query) {
            this.ser.mapper.decodeOne(resultSet = it, kclass = OUT::class)
        }
    }

    /**
     * Executes a custom SQL query with input and output.
     * This function is not inline like other [get] function because kotlin compiler
     * does not know how to recognize type of generic argument from the usage.
     * The problem was already [reported to JetBrains team.](https://youtrack.jetbrains.com/issue/KT-66286/Diamond-operator-becomes-too-big-for-inline-reified-functions).
     *
     * @param output The [KClass] representing the desired class of the objects in the result list.
     * @param input The input object used to generate the query.
     * @param buildSql A function that takes a [QueryBuilder] and returns the user provided SQL statement to be executed.
     * @return A list of objects of type [OUT], obtained by decoding each row of the result set.
     */
    public fun <IN : Any, OUT : Any> get(output: KClass<OUT>, input: IN, buildSql: (queryBuilder: QueryBuilder<IN>) -> String): List<OUT> {
        val query = this.ser.queryWithInput(input = input, buildSql = buildSql)
        return this.driver.query(query = query) {
            this.ser.mapper.decodeOne(resultSet = it, kclass = output)
        }
    }
}
