package com.urosjarc.dbmessiah.queries

import com.urosjarc.dbmessiah.Driver
import com.urosjarc.dbmessiah.Serializer
import com.urosjarc.dbmessiah.exceptions.DriverException
import com.urosjarc.dbmessiah.impl.derby.DerbySerializer
import com.urosjarc.dbmessiah.impl.h2.H2Serializer
import kotlin.reflect.KClass

/**
 * Class that provides various row queries for a database table.
 *
 * @param ser The serializer to use for serialization and deserialization of objects.
 * @param driver The database driver to use for executing queries.
 */
public open class RowQueries(
    public val ser: Serializer,
    public val driver: Driver
) {
    /**
     * Retrieves a single object from the specified table based on the primary key.
     *
     * @param table The class representing the table to select from.
     * @param pk The primary key value of the object to retrieve.
     * @return The object retrieved from the table, or null if no matching object is found.
     */
    public inline fun <reified T : Any> select(pk: Any): T? {
        val query = this.ser.selectTable(table = T::class, pk = pk)
        return this.driver.query(query = query) {
            this.ser.mapper.decodeOne(resultSet = it, kclass = T::class)
        }.firstOrNull()
    }

    /**
     * Inserts a row into the table.
     *
     * If [row] does not exists in the table its primary key will be inited with the assigned database value.
     *
     * @param row The object representing the row to be inserted.
     * @return true if the row was successfully inserted, false otherwise.
     */
    public open fun <T : Any> insert(row: T): Boolean {
        val T = this.ser.mapper.getTableInfo(obj = row)

        //If object has pk then reject it since its allready identified
        if (T.primaryKey.autoInc && T.primaryKey.getValue(obj = row) != null) return false //Only objects who doesnt have primary key can be inserted!!!

        //Insert it
        val query = this.ser.insertRow(row = row, batch = false)

        if (T.primaryKey.autoInc) {
            val pk =
                /**
                 * Derby and every database (not oracle) supports only Statement.RETURN_GENERATED_KEYS
                 * H2 don't have option to escape columns that JDBC needs to return so we will use Statement.RETURN_GENERATED_KEYS instead.
                 */
                if (this.ser is DerbySerializer || this.ser is H2Serializer)
                    this.driver.insert(query = query, primaryKey = null, onGeneratedKeysFail = this.ser.selectLastId)
                /**
                 * Oracle and every database (not derby) supports returning columns by name directly.
                 */
                else
                    this.driver.insert(query = query, primaryKey = this.ser.escaped(T.primaryKey.name), onGeneratedKeysFail = this.ser.selectLastId)

            //If pk didn't retrieved insert didn't happend
            if (pk == null) return false

            //Set primary key on object
            T.primaryKey.setValue(obj = row, value = pk)

            //Insert happend
            return true
        } else {
            //If we don't need to retrieve id joust update table
            return this.driver.update(query = query) == 1
        }
    }

    /**
     * Updates a row in the table by its primary key.
     *
     * @param row The object representing the row to be updated.
     * @return true if the row was successfully updated, false otherwise.
     * @throws DriverException if there is an error executing the update query.
     */
    public fun <T : Any> update(row: T): Boolean {
        val T = this.ser.mapper.getTableInfo(obj = row)

        //If object has not pk then reject since it must be first created
        if (T.primaryKey.getValue(obj = row) == null) return false //Only objects who doesnt have primary key can be inserted!!!

        //Update object
        val query = this.ser.updateRow(row = row)

        //Return number of updates
        val count = this.driver.update(query = query)

        //Success if only 1
        if (count == 1) return true
        else if (count == 0) return false
        else throw DriverException("Number of updated rows must be 1 or 0 but number of updated rows was: $count")
    }

    /**
     * Deletes a row from the table. By its primary key.
     * If row is deleted the primary key will reset to null value.
     *
     * @param row The object representing the row to be deleted.
     * @return true if the row was successfully deleted, false otherwise.
     * @throws DriverException if there is an error executing the delete query.
     */
    public fun <T : Any> delete(row: T): Boolean {
        val T = this.ser.mapper.getTableInfo(obj = row)

        //If object has not pk then reject since it must be first created
        if (T.primaryKey.getValue(obj = row) == null) return false //Only objects who doesnt have primary key can be inserted!!!

        //Delete object if primary key exists
        val query = this.ser.deleteRow(row = row)

        //Update rows and get change count
        val count = this.driver.update(query = query)

        //Success if only 1
        if (count == 0) return false
        else if (count == 1) {
            T.primaryKey.setValue(obj = row, value = null)
            return true
        } else throw DriverException("Number of deleted rows must be 1 or 0 but number of updated rows was: $count")
    }

    /**
     * Inserts a rows into the table with method [insert]. It should not be confused with batch insert!
     * Primary keys will be assigned in the process!
     *
     * If [row] does not exist in the table, its primary key will be initialized with the assigned database value.
     *
     * @param row The object representing the row to be inserted.
     * @return A list of booleans indicating whether each row was successfully inserted or not.
     */
    public fun <T : Any> insert(rows: Iterable<T>): List<Boolean> = rows.map { this.insert(row = it) }

    /**
     * Updates rows in the table by its primary key with method [update]. It should not be confused with batch update!
     *
     * @param rows The objects representing the rows to be updated.
     * @return A list of booleans indicating whether each row was successfully updated or not.
     */
    public fun <T : Any> update(rows: Iterable<T>): List<Boolean> = rows.map { this.update(row = it) }

    /**
     * Deletes rows in the table by its primary key with method [delete]. It should not be confused with batch delete!
     * Primary keys will be reseted to null values in the process!
     *
     * @param rows The objects representing the rows to be updated.
     * @return A list of booleans indicating whether each row was successfully updated or not.
     */
    public fun <T : Any> delete(rows: Iterable<T>): List<Boolean> = rows.map { this.delete(row = it) }
}
