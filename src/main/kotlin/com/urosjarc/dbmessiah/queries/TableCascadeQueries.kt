package com.urosjarc.dbmessiah.queries

import com.urosjarc.dbmessiah.Driver
import com.urosjarc.dbmessiah.Serializer
import com.urosjarc.dbmessiah.exceptions.DriverException

/**
 * Represents a class that provides methods for performing cascade operations on tables.
 *
 * @param ser The serializer used for table operations.
 * @param driver The driver used for executing queries.
 */
public class TableCascadeQueries(ser: Serializer, driver: Driver) : TableQueries(ser = ser, driver = driver) {
    /**
     * Drops the specified table with cascading enabled.
     *
     * @param table The table to be dropped.
     * @return The number of rows affected by the drop operation.
     */
    public inline fun <reified T : Any> dropCascade(throws: Boolean = true): Int {
        val query = this.ser.dropTable(table = T::class, cascade = true)
        try {
            return this.driver.update(query = query)
        } catch (e: DriverException) {
            if (throws) throw e
            return 0
        }
    }
}
