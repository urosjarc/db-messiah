package com.urosjarc.dbmessiah.queries

import com.urosjarc.dbmessiah.Driver
import com.urosjarc.dbmessiah.Serializer
import com.urosjarc.dbmessiah.domain.Cursor
import com.urosjarc.dbmessiah.domain.Page
import com.urosjarc.dbmessiah.exceptions.DriverException
import kotlin.reflect.KClass

/**
 * Provides methods to perform various operations on single database table.
 */
public open class TableQueries(
    public val ser: Serializer,
    public val driver: Driver
) {
    /**
     * Drops a database table specified by the given Kotlin class representation.
     *
     * @param T The Kotlin class representing the database table to be dropped.
     * @param throws Flag indicating whether to throw an exception if an error occurs while dropping the table. The default value is true.
     * @return The number of affected rows.
     */
    public inline fun <reified T : Any> drop(throws: Boolean = true): Int {
        val query = this.ser.dropTable(table = T::class)
        try {
            return this.driver.update(query = query)
        } catch (e: DriverException) {
            if(throws) throw e
            return 0
        }
    }

    /**
     * Creates a new table in the database based on the given Kotlin class representation.
     *
     * @param T The Kotlin class representing the database table to be created.
     * @param throws Flag indicating whether to throw an exception if an error occurs while creating the table. The default value is true.
     * @return The number of affected rows.
     */
    public inline fun <reified T : Any> create(throws: Boolean = true): Int {
        val query = this.ser.createTable(table = T::class)
        try {
            return this.driver.update(query = query)
        } catch (e: DriverException) {
            if(throws) throw e
            return 0
        }
    }

    /**
     * Deletes all rows from the specified table.
     *
     * @param T the class representing the table to select from
     * @return The number of rows affected by the delete operation.
     */
    public inline fun <reified T : Any> delete(): Int {
        val query = this.ser.deleteTable(table = T::class)
        return this.driver.update(query = query)
    }

    /**
     * Selects all table rows.
     *
     * @param T the class representing the table to select from
     * @return a list of objects representing the selected rows
     */
    public inline fun <reified T : Any> select(): List<T> {
        val query = this.ser.selectTable(table = T::class)
        return this.driver.query(query = query) {
            this.ser.mapper.decodeOne(resultSet = it, kclass = T::class)
        }
    }

    /**
     * Select table rows with offset pagination.
     * Use this method on small tables. For big tables use [Cursor] pagination.
     *
     * @param T The class representing the table to select from.
     * @param page The [Page] configuration for fetching items.
     * @return A list of objects representing the selected page.
     */
    public inline fun <reified T : Any> select(page: Page<T>): List<T> {
        val query = this.ser.selectTable(table = T::class, page = page)
        return this.driver.query(query = query) {
            this.ser.mapper.decodeOne(resultSet = it, kclass = T::class)
        }
    }

    /**
     * Select table rows with [Cursor] pagination.
     * Use this method on big tables.
     *
     * @param table The class representing the table to select from.
     * @param cursor The [Cursor] configuration for fetching items.
     * @return A list of objects representing the selected rows.
     */
    public inline fun <reified T: Any, V: Comparable<V>> select(cursor: Cursor<T, V>): List<T> {
        val query = this.ser.selectTable(table = T::class, cursor = cursor)
        return this.driver.query(query = query) {
            this.ser.mapper.decodeOne(resultSet = it, kclass = T::class)
        }
    }

}
