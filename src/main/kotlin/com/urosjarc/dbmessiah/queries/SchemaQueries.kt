package com.urosjarc.dbmessiah.queries

import com.urosjarc.dbmessiah.Driver
import com.urosjarc.dbmessiah.Schema
import com.urosjarc.dbmessiah.Serializer

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
     * Drops the specified schema from the database.
     *
     * @param schema The schema to be dropped.
     * @return The number of rows affected.
     */
    public fun drop(schema: Schema): Int {
        val query = this.ser.dropSchema(schema = schema)
        return this.driver.update(query = query)
    }

    /**
     * Creates a new schema in the database using the provided schema object.
     *
     * @param schema The schema object representing the schema to be created.
     *
     * @return The number of rows affected.
     */
    public fun create(schema: Schema): Int {
        val query = this.ser.createSchema(schema = schema)
        return this.driver.update(query = query)
    }

}
