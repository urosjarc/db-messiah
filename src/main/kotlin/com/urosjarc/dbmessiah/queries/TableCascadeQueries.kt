package com.urosjarc.dbmessiah.queries

import com.urosjarc.dbmessiah.Driver
import com.urosjarc.dbmessiah.Serializer
import kotlin.reflect.KClass

public class TableCascadeQueries(ser: Serializer, driver: Driver) : TableQueries(ser = ser, driver = driver) {
    public fun <T : Any> dropCascade(table: KClass<T>): Int {
        val query = this.ser.dropQuery(kclass = table, cascade = true)
        return this.driver.update(query = query)
    }
}
