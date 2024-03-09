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
        //If user provided to us empty list we stop the proces.
        val firstRow = rows.firstOrNull() ?: return 0

        //Get table information
        val T = this.ser.mapper.getTableInfo(obj = firstRow)
        val RB = T.getInsertRowBuilder()

        //Get SQL statement for insertion
        val query = this.ser.insertRow(row = firstRow, batch = true)

        //Execute query
        val batchQuery = BatchQuery(sql = query.sql, valueMatrix = rows.map { RB.queryValues(obj = it).toList() })

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
        //If user provided to us empty list we stop the proces.
        val firstRow = rows.firstOrNull() ?: return 0

        //Get table information
        val T = this.ser.mapper.getTableInfo(obj = firstRow)
        val RB = T.getUpdateRowBuilder()

        //Get SQL statement for updating
        val query = this.ser.updateRow(row = firstRow)

        //Build QueryValue matrix
        val valueMatrix = rows.map { listOf(*RB.queryValues(obj = it), T.primaryColumn.queryValue(obj = it)) }

        //Build batch query
        val batchQuery = BatchQuery(sql = query.sql, valueMatrix = valueMatrix)

        //Return result
        return this.driver.batch(batchQuery = batchQuery)
    }

    /**
     * Deletes a batch of objects from the database.
     *
     * @param rows The iterable collection of objects to be deleted.
     * @return The number of objects deleted.
     */
    public fun <T : Any> delete(rows: Iterable<T>): Int {
        //If user provided to us empty list we stop the proces.
        val firstRow = rows.firstOrNull() ?: return 0

        //Get table information
        val T = this.ser.mapper.getTableInfo(obj = firstRow)

        //Get SQL statement for deleting
        val query = this.ser.deleteRow(row = firstRow)

        //Build QueryValue matrix
        val valueMatrix = rows.map { listOf(T.primaryColumn.queryValue(obj = it)) }

        //Build batch query
        val batchQuery = BatchQuery(sql = query.sql, valueMatrix = valueMatrix)

        //Return number of rows modified
        return this.driver.batch(batchQuery = batchQuery)
    }
}
