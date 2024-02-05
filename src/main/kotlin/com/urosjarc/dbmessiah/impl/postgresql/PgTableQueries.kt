package com.urosjarc.dbmessiah.impl.postgresql

import com.urosjarc.dbmessiah.Driver
import com.urosjarc.dbmessiah.Serializer
import com.urosjarc.dbmessiah.domain.queries.TableQueries
import kotlin.reflect.KClass

class PgTableQueries(ser: Serializer, driver: Driver) : TableQueries(ser = ser, driver = driver) {
    fun <T : Any> dropCascade(table: KClass<T>): Int {
        val query = this.ser.dropQuery(kclass = table, cascade = true)
        return this.driver.update(query = query)
    }
}
