package com.urosjarc.dbmessiah

import com.urosjarc.dbmessiah.domain.queries.Page
import com.urosjarc.dbmessiah.domain.queries.QueryBuilder
import kotlin.reflect.KClass

interface Service {

    val ser: Serializer
    val eng: Engine

    /**
     * Table manipulation
     */
    fun <T : Any> drop(kclass: KClass<T>): Boolean
    fun <T : Any> create(kclass: KClass<T>): Boolean


    /**
     * Updates
     */
    fun <T : Any> insert(obj: T): Boolean
    fun <T : Any> update(obj: T): Boolean
    fun <T : Any> delete(obj: T): Boolean

    /**
     * Selects
     */
    fun <T : Any> select(kclass: KClass<T>): List<T>
    fun <T : Any, K: Any> select(kclass: KClass<T>, pk: K): T?
    fun <T : Any> select(kclass: KClass<T>, page: Page<T>): List<T>

    /**
     * Generic queries
     */
    fun <T : Any> query(output: KClass<T>, getSql: () -> String): List<T>
    fun <T : Any> query(input: T, output: KClass<T>, getSql: (queryBuilder: QueryBuilder<T>) -> String): List<T>
}
