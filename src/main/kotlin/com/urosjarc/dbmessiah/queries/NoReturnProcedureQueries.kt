package com.urosjarc.dbmessiah.queries

import com.urosjarc.dbmessiah.Driver
import com.urosjarc.dbmessiah.Serializer
import com.urosjarc.dbmessiah.builders.ProcedureBuilder
import com.urosjarc.dbmessiah.exceptions.DriverException

/**
 * Class responsible for executing database queries related stored procedures
 * that return no values.
 *
 * @param ser The serializer to be used for object serialization.
 * @param driver The database driver to be used for executing queries.
 */
public open class NoReturnProcedureQueries(
    public val ser: Serializer,
    public val driver: Driver
) {
    /**
     * Drops a procedure of type T from the database.
     *
     * @param throws Whether or not to throw a [DriverException] if an error occurs. Default is true.
     * @return The number of affected rows after dropping the procedure.
     */
    public inline fun <reified T : Any> drop(throws: Boolean = true): Int {
        val query = this.ser.dropProcedure(procedure = T::class)
        try {
            return this.driver.update(query = query)
        } catch (e: DriverException) {
            if (throws) throw e
            return 0
        }
    }

    /**
     * Creates a procedure of type T and executes it using the provided procedure builder.
     *
     * @param throws Determines whether to throw a [DriverException] if an error occurs during execution. Default is true.
     * @param body The procedure builder function that provides the procedure body.
     * @return The number of rows affected by the execution of the procedure.
     */
    public inline fun <reified T : Any> create(throws: Boolean = true, body: (ProcedureBuilder<T>) -> String): Int {
        val query = this.ser.createProcedure(procedure = T::class, procedureBody = body(ProcedureBuilder(ser = ser, procedure = T::class)))
        try {
            return this.driver.update(query = query)
        } catch (e: DriverException) {
            if (throws) throw e
            return 0
        }
    }

    /**
     * Calls a stored procedure with the given procedure object.
     * Executes the procedure and updates the database.
     *
     * @param procedure The procedure object to be executed.
     * @param T The type of the procedure object.
     */
    public open fun <T : Any> call(procedure: T) {
        val query = this.ser.callProcedure(procedure = procedure)
        this.driver.update(query = query)
    }
}
