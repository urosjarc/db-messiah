package com.urosjarc.dbmessiah.queries

import com.urosjarc.dbmessiah.Driver
import com.urosjarc.dbmessiah.Serializer
import com.urosjarc.dbmessiah.domain.Cursor
import com.urosjarc.dbmessiah.domain.Page
import kotlin.reflect.KClass

/**
 * Provides methods to perform various operations on single database table.
 * It takes a Serializer and a Driver as dependencies, which are used for query generation and execution.
 */
public open class TableQueries(
    protected val ser: Serializer,
    protected val driver: Driver
) {
    /**
     * Drops a database table.
     *
     * @param table the class representing the table to be dropped
     * @return the number of affected rows
     */
    public open fun <T : Any> drop(table: KClass<T>): Int {
        val query = this.ser.dropQuery(kclass = table)
        return this.driver.update(query = query)
    }

    /**
     * Creates a new table.
     *
     * @param table the class representing the table to be created.
     * @return the number of affected rows
     */
    public open fun <T : Any> create(table: KClass<T>): Int {
        val query = this.ser.createQuery(kclass = table)
        return this.driver.update(query = query)
    }

    /**
     * Deletes all rows from the specified table.
     *
     * @param table the table to delete records from
     * @return the number of records deleted
     */
    public fun <T : Any> delete(table: KClass<T>): Int {
        val query = this.ser.deleteQuery(kclass = table)
        return this.driver.update(query = query)
    }

    /**
     * Selects all table rows.
     *
     * @param table the class representing the table to select from
     * @return a list of objects representing the selected rows
     */
    public fun <T : Any> select(table: KClass<T>): List<T> {
        val query = this.ser.query(kclass = table)
        return this.driver.query(query = query) {
            this.ser.mapper.decode(resultSet = it, kclass = table)
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
    public fun <T : Any> select(table: KClass<T>, page: Page<T>): List<T> {
        val query = this.ser.query(kclass = table, page = page)
        return this.driver.query(query = query) {
            this.ser.mapper.decode(resultSet = it, kclass = table)
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
    public fun <T: Any, V: Comparable<V>> select(table: KClass<T>, cursor: Cursor<T, V>): List<T> {
        val query = this.ser.query(kclass = table, cursor = cursor)
        return this.driver.query(query = query) {
            this.ser.mapper.decode(resultSet = it, kclass = table)
        }
    }
}
