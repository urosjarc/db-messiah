package com.urosjarc.dbmessiah

import com.urosjarc.dbmessiah.domain.queries.Page
import com.urosjarc.dbmessiah.domain.queries.QueryBuilder
import kotlin.reflect.KClass

interface Service {

    val ser: Serializer
    val eng: Engine

    fun <T : Any> drop(kclass: KClass<T>): Int
    fun <T : Any> create(kclass: KClass<T>): Int
    fun <T : Any> select(kclass: KClass<T>): List<T>
    fun <T : Any> selectPage(kclass: KClass<T>, page: Page<T>): List<T>
    fun <T : Any> insert(obj: T): Boolean
    fun <T : Any> update(obj: T): Boolean
    fun <T : Any> query(kclass: KClass<T>, getSql: (queryBuilder: QueryBuilder<T>) -> String): List<T>
}
