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
public open class SchemaQueries(
    private val ser: Serializer,
    private val driver: Driver
) {
    /**
     * Drops a database schema.
     *
     * @param schema The schema to be dropped.
     * @param throws Flag indicating whether to throw an exception on error. Default is true.
     * @return The number of rows affected.
     */
    public fun drop(schema: Schema, throws: Boolean = true): Int {
        val query = this.ser.dropSchema(schema = schema)
        try {
            return this.driver.update(query = query)
        } catch (e: DriverException) {
            if (throws) throw e
            return 0
        }
    }

    /**
     * Creates a new schema in the database using the provided schema object.
     *
     * @param schema The schema object representing the schema to be created.
     * @param throws Flag indicating whether to throw an exception on error or not. Default is true.
     * @return The number of rows affected.
     */
    public fun create(schema: Schema, throws: Boolean = true): Int {
        val query = this.ser.createSchema(schema = schema)
        try {
            return this.driver.update(query = query)
        } catch (e: DriverException) {
            if (throws) throw e
            return 0
        }
    }

    /**
     * Creates a new schema in the database using the provided schema object.
     *
     * @param schema The schema object representing the schema to be created.
     * @param throws Flag indicating whether to throw an exception on error or not. Default is true.
     * @return The number of rows affected.
     */
    public fun create(schema: String, throws: Boolean = true): Int = this.create(schema = Schema(name = schema, tables = listOf()), throws = throws)

}
