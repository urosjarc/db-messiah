package com.urosjarc.dbmessiah.impl.oracle

import com.urosjarc.dbmessiah.Driver
import com.urosjarc.dbmessiah.Serializer
import com.urosjarc.dbmessiah.domain.queries.TableQueries
import com.urosjarc.dbmessiah.domain.table.Page
import kotlin.reflect.KClass

open class OracleTableQueries(ser: Serializer, driver: Driver): TableQueries(ser = ser, driver = driver) {
    override fun <T : Any> create(table: KClass<T>): Int {
        val query = this.ser.createQuery(kclass = table)
        this.driver.execute(query = query) { i, rs ->
            listOf()
        }
        return 1
    }
}
