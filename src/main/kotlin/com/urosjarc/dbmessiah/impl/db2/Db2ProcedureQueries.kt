package com.urosjarc.dbmessiah.impl.db2

import com.urosjarc.dbmessiah.Driver
import com.urosjarc.dbmessiah.Serializer
import com.urosjarc.dbmessiah.exceptions.DriverException
import kotlin.reflect.KClass

/**
 * Class responsible for executing database queries related to calling stored procedures.
 *
 * @param ser The serializer to be used for object serialization.
 * @param driver The database driver to be used for executing queries.
 */
public class Db2ProcedureQueries(
    private val ser: Serializer,
    private val driver: Driver
) {
    public fun <T : Any> drop(procedure: KClass<T>, throws: Boolean = true): Int {
        val query = this.ser.dropProcedure(procedure = procedure)
        try {
            return this.driver.update(query = query)
        } catch (e: DriverException) {
            if (throws) throw e
            return 0
        }
    }

    public fun <T : Any> create(procedure: KClass<T>, throws: Boolean = true, body: () -> String): Int {
        val query = this.ser.createProcedure(procedure = procedure, body = body)
        try {
            return this.driver.update(query = query)
        } catch (e: DriverException) {
            if (throws) throw e
            return 0
        }
    }

    public fun <T : Any> call(procedure: T) {
        val query = this.ser.callProcedure(procedure = procedure)
        this.driver.update(query = query)
    }
}
