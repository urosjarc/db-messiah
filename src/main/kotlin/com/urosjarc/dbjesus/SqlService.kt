package com.urosjarc.dbjesus

import com.zaxxer.hikari.HikariConfig
import kotlin.reflect.KClass

abstract class SqlService(config: HikariConfig) : SqlEngine(config = config) {
    abstract fun <T : Any> createTable(kclass: KClass<T>): String
    abstract fun <T : Any> selectTable(cls: KClass<T>): MutableList<T>
    abstract fun insertTable(obj: Any)
    abstract fun updateTable(obj: Any)
    abstract fun <T : Any> selectTable(cls: KClass<T>, sql: String): MutableList<T>
}
