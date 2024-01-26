package com.urosjarc.dbjesus

import com.urosjarc.dbjesus.domain.queries.InsertQuery
import com.urosjarc.dbjesus.domain.queries.Page
import com.urosjarc.dbjesus.domain.queries.Query
import com.urosjarc.dbjesus.domain.queries.QueryBuilder
import kotlin.reflect.KClass

interface Serializer {

    val mapper: Mapper

    fun <T : Any> createQuery(kclass: KClass<T>): Query
    fun <T : Any> selectAllQuery(kclass: KClass<T>): Query
    fun <T : Any> selectPageQuery(kclass: KClass<T>, page: Page<T>): Query
    fun <T : Any, K: Any> selectOneQuery(kclass: KClass<T>, pkValue: K): Query
    fun insertQuery(obj: Any): InsertQuery
    fun updateQuery(obj: Any): Query
    fun <T : Any> query(sourceObj: T, getSql: (queryBuilder: QueryBuilder) -> String): Query

}
