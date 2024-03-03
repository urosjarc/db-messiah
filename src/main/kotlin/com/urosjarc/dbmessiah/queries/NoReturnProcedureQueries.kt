package com.urosjarc.dbmessiah.queries

import com.urosjarc.dbmessiah.Driver
import com.urosjarc.dbmessiah.Serializer
import com.urosjarc.dbmessiah.builders.ProcedureBuilder
import com.urosjarc.dbmessiah.exceptions.DriverException

/**
 * Class responsible for executing database queries related to calling stored procedures.
 *
 * @param ser The serializer to be used for object serialization.
 * @param driver The database driver to be used for executing queries.
 */
public open class NoReturnProcedureQueries(
    public val ser: Serializer,
    public val driver: Driver
) {
    public inline fun <reified T : Any> drop(throws: Boolean = true): Int {
        val query = this.ser.dropProcedure(procedure = T::class)
        try {
            return this.driver.update(query = query)
        } catch (e: DriverException) {
            if (throws) throw e
            return 0
        }
    }

    public inline fun <reified T : Any> create(throws: Boolean = true, body: (ProcedureBuilder<T>) -> String): Int {
        val query = this.ser.createProcedure(procedure = T::class, procedureBody = body(ProcedureBuilder(ser = ser, procedure = T::class)))
        try {
            return this.driver.update(query = query)
        } catch (e: DriverException) {
            if (throws) throw e
            return 0
        }
    }

    public open fun <T : Any> call(procedure: T) {
        val query = this.ser.callProcedure(procedure = procedure)
        this.driver.update(query = query)
    }
}
