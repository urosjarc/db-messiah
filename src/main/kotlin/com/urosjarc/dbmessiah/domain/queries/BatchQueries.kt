package com.urosjarc.dbmessiah.domain.queries

import com.urosjarc.dbmessiah.Driver
import com.urosjarc.dbmessiah.Serializer
import com.urosjarc.dbmessiah.domain.querie.BatchQuery

class BatchQueries(val ser: Serializer, val driver: Driver) {
    fun <T : Any> insertBatch(rows: Iterable<T>): Int {
        val obj = rows.firstOrNull() ?: return 0

        val T = this.ser.mapper.getTableInfo(obj = obj)

        //Filter only those whos primary key is null
        val fobjs = rows.filter { T.primaryKey.getValue(it) == null }

        //If no object has free primary key then finish
        if (fobjs.isEmpty()) return 0

        //Insert it to db
        val query = this.ser.insertQuery(obj = fobjs[0], batch = true)

        //Execute query
        val batchQuery = BatchQuery(sql = query.sql, valueMatrix = fobjs.map { T.queryValues(obj = it).toList() })

        //Return count of updated elements
        return this.driver.batch(batchQuery = batchQuery)
    }

    fun <T : Any> updateBatch(rows: Iterable<T>): Int {
        val obj = rows.firstOrNull() ?: return 0

        val T = this.ser.mapper.getTableInfo(obj = obj)

        //Filter only those whos primary key is null
        val fobjs = rows.filter { T.primaryKey.getValue(it) != null }

        //If no object has free primary key then finish
        if (fobjs.isEmpty()) return 0

        //Update objects
        val query = this.ser.updateQuery(obj = fobjs[0])
        val valueMatrix = fobjs.map { listOf(*T.queryValues(obj = it), T.primaryKey.queryValue(obj = it)) }
        val batchQuery = BatchQuery(sql = query.sql, valueMatrix = valueMatrix)

        //Return result
        return this.driver.batch(batchQuery = batchQuery)
    }

    fun <T : Any> deleteBatch(rows: Iterable<T>): Int {
        val obj = rows.firstOrNull() ?: return 0

        val T = this.ser.mapper.getTableInfo(obj = obj)

        //Filter only those whos primary key is not null
        val fobjs = rows.filter { T.primaryKey.getValue(it) != null }

        //If no object has free primary key then finish
        if (fobjs.isEmpty()) return 0

        //Delete objects
        val query = this.ser.deleteQuery(obj = fobjs[0])
        val valueMatrix = fobjs.map { listOf(T.primaryKey.queryValue(obj = it)) }
        val batchQuery = BatchQuery(sql = query.sql, valueMatrix = valueMatrix)

        //Return result
        val count = this.driver.batch(batchQuery = batchQuery)

        //Reset primary keys so that objects become invalid
        rows.forEach { T.primaryKey.setValue(obj = it, null) }

        return count
    }
}
