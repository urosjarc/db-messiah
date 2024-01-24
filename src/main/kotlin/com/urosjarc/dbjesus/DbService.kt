package com.urosjarc.dbjesus

import com.urosjarc.dbjesus.domain.Encoders
import com.urosjarc.dbjesus.domain.Page
import com.urosjarc.dbjesus.domain.Query
import kotlin.reflect.KClass

interface DbService<ID_TYPE: Any> {

    val ser: DbSerializer<ID_TYPE>
    val eng: DbEngine<ID_TYPE>
    fun createTable(kclass: KClass<Any>): Int
    fun <T : Any> selectTable(kclass: KClass<T>): List<T>
    fun <T : Any> selectTablePage(kclass: KClass<T>, page: Page<T>): List<T>
    fun <T : Any> insertTable(obj: T): ID_TYPE?
    fun updateTable(obj: Any): Int
    fun <T : Any> query(kclass: KClass<T>, getEscapedQuery: (encoders: Encoders) -> Query): List<T>
}
