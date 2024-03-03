package com.urosjarc.dbmessiah.builders

import com.urosjarc.dbmessiah.Serializer
import com.urosjarc.dbmessiah.exceptions.QueryException
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1


/**
 * The [ProcedureBuilder] helps build SQL queries for the body of stored procedures.
 *
 * @param T The type of the procedure class.
 * @property ser The [Serializer] used to help with serializing and escaping process.
 * @property procedure The class representing the stored procedure.
 */
public open class ProcedureBuilder<T : Any>(ser: Serializer, public val procedure: KClass<T>) : SqlBuilder(ser = ser) {

    /**
     * Returns type safe escaped string representation of the provided property `kp`.
     *
     * @param kprop the property to be serialized.
     * @return the type safe escaped string representation of the procedure property.
     */
    public fun arg(kprop: KProperty1<T, *>): String = this.ser.escaped(procedureArg = kprop)

}
