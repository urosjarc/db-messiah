package com.urosjarc.dbjesus

import com.urosjarc.dbjesus.domain.Page
import com.urosjarc.dbjesus.domain.Query
import com.urosjarc.dbjesus.domain.QueryBuilder
import kotlin.reflect.KClass

interface Service<PK : Any> {

    val ser: Serializer
    val eng: Engine

    fun <T : Any> createTable(kclass: KClass<T>): Int
    fun <T : Any> selectTable(kclass: KClass<T>): List<T>
    fun <T : Any> selectTablePage(kclass: KClass<T>, page: Page<T>): List<T>
    fun <T : Any> insertTable(obj: T): PK?
    fun <T : Any> updateTable(obj: T): Int
    fun <T : Any> query(kclass: KClass<T>, getEscapedQuery: (encoders: QueryBuilder) -> Query): List<T>
}
