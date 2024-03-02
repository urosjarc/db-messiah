package com.urosjarc.dbmessiah.queries

import com.urosjarc.dbmessiah.Driver
import com.urosjarc.dbmessiah.Serializer
import com.urosjarc.dbmessiah.domain.Cursor
import com.urosjarc.dbmessiah.domain.Page
import com.urosjarc.dbmessiah.exceptions.DriverException
import kotlin.reflect.KClass

/**
 * Provides methods to perform various operations on single database table.
 * It takes a Serializer and a Driver as dependencies, which are used for query generation and execution.
 */
public open class TableQueries(
    public val ser: Serializer,
    public val driver: Driver
) {
    /**
     * Drops a database table.
     *
     * @param table The Kotlin class representing the table to be dropped.
     * @param throws Flag indicating whether to throw an exception if an error occurs while dropping the table.
     *               The default value is true.
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
     * Creates a new row in the database table specified by the given Kotlin class representation.
     *
     * @param table The Kotlin class representing the database table to which the row will be created.
     * @param throws Flag indicating whether to throw an exception if an error occurs while creating the row.
     *               The default value is true.
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
     * @param table the table to delete records from
     * @return the number of records deleted
     */
    public inline fun <reified T : Any> delete(): Int {
        val query = this.ser.deleteTable(table = T::class)
        return this.driver.update(query = query)
    }

    /**
     * Selects all table rows.
     *
     * @param table the class representing the table to select from
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
     * Use this method on small tables. For big tables use Cursor pagination.
     *
     * @param table The class representing the table to select from.
     * @param page The page configuration for fetching items.
     * @return A list of objects representing the selected page.
     */
    public inline fun <reified T : Any> select(page: Page<T>): List<T> {
        val query = this.ser.selectTable(table = T::class, page = page)
        return this.driver.query(query = query) {
            this.ser.mapper.decodeOne(resultSet = it, kclass = T::class)
        }
    }

    /**
     * Select table rows with cursor pagination.
     * Use this method on big tables.
     *
     * @param table The class representing the table to select from.
     * @param cursor The cursor configuration for fetching items.
     * @return A list of objects representing the selected rows.
     */
    public inline fun <reified T: Any, V: Comparable<V>> select(cursor: Cursor<T, V>): List<T> {
        val query = this.ser.selectTable(table = T::class, cursor = cursor)
        return this.driver.query(query = query) {
            this.ser.mapper.decodeOne(resultSet = it, kclass = T::class)
        }
    }

}
