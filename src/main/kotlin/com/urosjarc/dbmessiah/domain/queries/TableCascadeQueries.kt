package com.urosjarc.dbmessiah.domain.queries

import com.urosjarc.dbmessiah.Driver
import com.urosjarc.dbmessiah.Serializer
import kotlin.reflect.KClass

class TableCascadeQueries(ser: Serializer, driver: Driver) : TableQueries(ser = ser, driver = driver) {
    fun <T : Any> dropCascade(table: KClass<T>): Int {
        val query = this.ser.dropQuery(kclass = table, cascade = true)
        return this.driver.update(query = query)
    }
}