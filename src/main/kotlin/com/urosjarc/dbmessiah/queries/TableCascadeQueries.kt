package com.urosjarc.dbmessiah.queries

import com.urosjarc.dbmessiah.Driver
import com.urosjarc.dbmessiah.Serializer
import kotlin.reflect.KClass

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
    public fun <T : Any> dropCascade(table: KClass<T>): Int {
        val query = this.ser.dropQuery(kclass = table, cascade = true)
        return this.driver.update(query = query)
    }
}
