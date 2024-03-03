package com.urosjarc.dbmessiah.builders

import com.urosjarc.dbmessiah.Mapper
import com.urosjarc.dbmessiah.Serializer
import com.urosjarc.dbmessiah.data.Query
import com.urosjarc.dbmessiah.data.QueryValue
import com.urosjarc.dbmessiah.exceptions.MappingException
import kotlin.reflect.KProperty1


/**
 * The [ProcedureBuilder] class is responsible for building custom SQL [Query].
 *
 * @param IN The type of [input].
 * @property input The class where input values will be located.
 * @property mapper The [Mapper] object to help in serialization process.
 *
 * @throws MappingException if the [input] class is not registered in global inputs.
 */
public open class QueryBuilder<IN : Any>(
    public val input: IN,
    ser: Serializer
) : QueryEscaper(ser = ser) {

    /**
     * Used within the class to accumulate query values while the custom SQL query is being built by the user.
     * It is populated through the [input] function, which takes a [KProperty1] object representing a property of the input class,
     * and maps it to a corresponding [QueryValue].
     */
    private val queryValues: MutableList<QueryValue> = mutableListOf()

    init {
        if (!this.ser.mapper.isRegistered(kclass = this.input::class))
            throw MappingException("Input class '${this.input::class.simpleName}' is not registered in global inputs")
    }

    /**
     * Final result of this [ProcedureBuilder] containing final SQL string and list of [QueryValue].
     *
     * @param sql The SQL query.
     * @return The [Query] object created.
     */
    internal fun build(sql: String) = Query(sql = sql, values = this.queryValues.toTypedArray())

    /**
     * Retrieve the serialized value of a [input] property and adds it as a [QueryValue] to the [queryValues].
     * This method is to be used inside SQL template.
     *
     * @param kp The [KProperty1] which should be injected in SQL string.
     * @return The placeholder character "?" indicating the serialized value will be replaced by [QueryValue] in the [queryValues].
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
