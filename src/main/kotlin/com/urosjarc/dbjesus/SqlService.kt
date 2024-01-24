package com.urosjarc.dbjesus

import com.urosjarc.dbjesus.sqlite.SqliteEngine
import kotlin.reflect.KClass

interface SqlService<ID> {
    val serializer: SqlSerializer<ID>
    val engine: SqliteEngine
    fun createTable(kclass: KClass<Any>): Int
    fun <T : Any> selectTable(kclass: KClass<T>): List<T>
    fun insertTable(obj: Any): Int
    fun updateTable(obj: Any): Int
    fun queryMany(sql: String, vararg kClass: KClass<Any>): List<Any>
    fun <T : Any> query(sql: String, kClass: KClass<T>): List<T>
}
