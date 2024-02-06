package com.urosjarc.dbmessiah.domain.queries

import com.urosjarc.dbmessiah.Driver
import com.urosjarc.dbmessiah.Schema
import com.urosjarc.dbmessiah.Serializer

open class SchemaQueries(val ser: Serializer, val driver: Driver) {
    fun drop(schema: Schema): Int {
        val query = this.ser.dropQuery(schema = schema)
        return this.driver.update(query = query)
    }

    fun create(schema: Schema): Int {
        val query = this.ser.createQuery(schema = schema)
        return this.driver.update(query = query)
    }

}
