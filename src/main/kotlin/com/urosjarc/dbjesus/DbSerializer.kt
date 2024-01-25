package com.urosjarc.dbjesus

import com.urosjarc.dbjesus.domain.Encoders
import com.urosjarc.dbjesus.domain.InsertQuery
import com.urosjarc.dbjesus.domain.Page
import com.urosjarc.dbjesus.domain.Query
import kotlin.reflect.KClass

interface DbSerializer<ID_TYPE> {
    val mapper: DbMapper
    fun <T : Any> createQuery(kclass: KClass<T>): Query
    fun <T : Any> selectAllQuery(kclass: KClass<T>): Query
    fun <T : Any> selectPageQuery(kclass: KClass<T>, page: Page<T>): Query
    fun <T : Any> selectOneQuery(kclass: KClass<T>, id: ID_TYPE): Query
    fun insertQuery(obj: Any): InsertQuery
    fun updateQuery(obj: Any): Query
    fun query(getEscapedQuery: (addEncoders: Encoders) -> Query): Query
}
