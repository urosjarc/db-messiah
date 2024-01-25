package com.urosjarc.dbjesus

import com.urosjarc.dbjesus.domain.InsertQuery
import com.urosjarc.dbjesus.domain.Page
import com.urosjarc.dbjesus.domain.Query
import com.urosjarc.dbjesus.domain.QueryBuilder
import kotlin.reflect.KClass

interface Serializer {

    val mapper: Mapper

    fun <T : Any> createQuery(kclass: KClass<T>): Query
    fun <T : Any> selectAllQuery(kclass: KClass<T>): Query
    fun <T : Any> selectPageQuery(kclass: KClass<T>, page: Page<T>): Query
    fun <T : Any, K: Any> selectOneQuery(kclass: KClass<T>, id: K): Query
    fun insertQuery(obj: Any): InsertQuery
    fun updateQuery(obj: Any): Query
    fun <T : Any> query(sourceObj: T, getSql: (queryBuilder: QueryBuilder) -> String): Query

}
