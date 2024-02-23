package com.urosjarc.dbmessiah.queries

import com.urosjarc.dbmessiah.Driver
import com.urosjarc.dbmessiah.Schema
import com.urosjarc.dbmessiah.Serializer

public open class SchemaQueries(
    private val ser: Serializer,
    private val driver: Driver
) {
    public fun drop(schema: Schema): Int {
        val query = this.ser.dropQuery(schema = schema)
        return this.driver.update(query = query)
    }

    public fun create(schema: Schema): Int {
        val query = this.ser.createQuery(schema = schema)
        return this.driver.update(query = query)
    }

}
