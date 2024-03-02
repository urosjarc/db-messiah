package com.urosjarc.dbmessiah.queries

import com.urosjarc.dbmessiah.Driver
import com.urosjarc.dbmessiah.Schema
import com.urosjarc.dbmessiah.Serializer
import com.urosjarc.dbmessiah.exceptions.DriverException

/**
 * Represents a class that provides methods for working with database schema queries.
 *
 * @property ser The Serializer instance used for query serialization.
 * @property driver The Driver instance used for executing the queries.
 */
public open class SchemaCascadeQueries(
    private val ser: Serializer,
    private val driver: Driver
) : SchemaQueries(ser = ser, driver = driver) {

    /**
     * Drops a database schema.
     *
     * @param schema The schema to be dropped.
     * @param throws Flag indicating whether to throw an exception on error. Default is true.
     * @return The number of rows affected.
     * @throws DriverException If there is an error processing the drop query.
     */
    public fun dropCascade(schema: Schema, throws: Boolean = true): Int {
        val query = this.ser.dropSchema(schema = schema, cascade = true)
        try {
            return this.driver.update(query = query)
        } catch (e: DriverException) {
            if (throws) throw e
            return 0
        }
    }
}
