package com.urosjarc.dbmessiah.queries

import com.urosjarc.dbmessiah.Driver
import com.urosjarc.dbmessiah.Serializer
import com.urosjarc.dbmessiah.data.BatchQuery

/**
 * Class for performing batch operations.
 *
 * @property ser The Serializer used for generating queries and mapping objects to tables in the database.
 * @property driver The Driver used for executing the batch queries.
 */
public class BatchQueries(
    private val ser: Serializer,
    private val driver: Driver
) {
    /**
     * Inserts a batch of objects into the database.
     * This method WILL not update objects primary keys!!!
     *
     * @param rows The iterable collection of objects to be inserted.
     * @return The number of objects inserted.
     */
    public fun <T : Any> insert(rows: Iterable<T>): Int {
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

    /**
     * Updates a batch of objects in the database.
     *
     * @param rows The iterable collection of objects to be updated.
     * @return The number of objects updated.
     */
    public fun <T : Any> update(rows: Iterable<T>): Int {
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

    /**
     * Deletes a batch of objects from the database.
     * This method WILL reset objects primary keys!!!
     *
     * @param rows The iterable collection of objects to be deleted.
     * @return The number of objects deleted.
     */
    public fun <T : Any> delete(rows: Iterable<T>): Int {
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
