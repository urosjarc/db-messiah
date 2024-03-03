package com.urosjarc.dbmessiah.builders

import com.urosjarc.dbmessiah.Mapper
import com.urosjarc.dbmessiah.Serializer
import com.urosjarc.dbmessiah.data.Query
import com.urosjarc.dbmessiah.data.QueryValue
import com.urosjarc.dbmessiah.exceptions.MappingException
import kotlin.reflect.KProperty1


/**
 * The [QueryBuilder] represents a builder for creating SQL queries which needs kotlin values to be injected inside.
 *
 * @param IN the input type for the query builder.
 * @property input the input object used for the query.
 * @constructor Creates an instance of [QueryBuilder] with the specified input and serializer.
 */
public open class QueryBuilder<IN : Any>(
    public val input: IN,
    ser: Serializer
) : SqlBuilder(ser = ser) {

    /**
     * List of [QueryValue] which holds information about injected values.
     */
    private val queryValues: MutableList<QueryValue> = mutableListOf()

    /**
     * Builds final SQL [Query] that needs to be executed.
     *
     * @param sql The final SQL string for the query.
     * @return A [Query] object representing the SQL query and its future injected values.
     */
    internal fun build(sql: String) = Query(sql = sql, values = this.queryValues.toTypedArray())

    /**
     * User can use this function to specify which property wants to inject to SQL string.
     *
     * @param kp The [KProperty1] representing the property to be injected.
     * @return The question mark placeholder representing the injected value in the SQL query.
     */
    public fun input(kp: KProperty1<IN, *>): String {
        val ser = this.ser.mapper.getSerializer(kp)

        val qv = QueryValue(
            name = kp.name,
            jdbcType = ser.jdbcType,
            encoder = ser.encoder,
            value = kp.get(receiver = this.input)
        )

        this.queryValues.add(qv)

        return "?"
    }

}
