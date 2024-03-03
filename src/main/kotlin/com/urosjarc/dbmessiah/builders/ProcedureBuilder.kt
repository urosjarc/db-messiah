package com.urosjarc.dbmessiah.builders

import com.urosjarc.dbmessiah.Serializer
import com.urosjarc.dbmessiah.data.Query
import com.urosjarc.dbmessiah.exceptions.MappingException
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1


public open class ProcedureBuilder<T : Any>(ser: Serializer, public val procedure: KClass<T>) : SqlBuilder(ser = ser) {

    init {
        if (!this.ser.mapper.isRegistered(kclass = this.procedure))
            throw MappingException("Procedure class '${this.procedure::class.simpleName}' is not registered in global procedures")
    }

    /**
     * Final result of this [ProcedureBuilder] containing final SQL string and list of [QueryValue].
     *
     * @param sql The SQL query.
     * @return The [Query] object created.
     */
    internal fun build(sql: String) = Query(sql = sql)

    /**
     * Retrieve the serialized value of a [input] property and adds it as a [QueryValue] to the [queryValues].
     * This method is to be used inside SQL template.
     *
     * @param kp The [KProperty1] which should be injected in SQL string.
     * @return The placeholder character "?" indicating the serialized value will be replaced by [QueryValue] in the [queryValues].
     */
    public fun arg(kp: KProperty1<T, *>): String = this.ser.escaped(procedureArg = kp)

}
