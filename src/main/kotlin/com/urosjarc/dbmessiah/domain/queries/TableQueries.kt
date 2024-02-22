package com.urosjarc.dbmessiah.domain.queries

import com.urosjarc.dbmessiah.Driver
import com.urosjarc.dbmessiah.Serializer
import com.urosjarc.dbmessiah.domain.table.Page
import kotlin.reflect.KClass

public open class TableQueries(
    protected val ser: Serializer,
    protected val driver: Driver
) {
    public open fun <T : Any> drop(table: KClass<T>): Int {
        val query = this.ser.dropQuery(kclass = table)
        return this.driver.update(query = query)
    }

    public open fun <T : Any> create(table: KClass<T>): Int {
        val query = this.ser.createQuery(kclass = table)
        return this.driver.update(query = query)
    }

    public fun <T : Any> delete(table: KClass<T>): Int {
        val query = this.ser.deleteQuery(kclass = table)
        return this.driver.update(query = query)
    }

    public fun <T : Any> select(table: KClass<T>): List<T> {
        val query = this.ser.query(kclass = table)
        return this.driver.query(query = query) {
            this.ser.mapper.decode(resultSet = it, kclass = table)
        }
    }

    public fun <T : Any> select(table: KClass<T>, page: Page<T>): List<T> {
        val query = this.ser.query(kclass = table, page = page)
        return this.driver.query(query = query) {
            this.ser.mapper.decode(resultSet = it, kclass = table)
        }
    }
}
