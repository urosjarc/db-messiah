package com.urosjarc.dbjesus

import com.urosjarc.dbjesus.domain.InsertQuery
import com.urosjarc.dbjesus.domain.Query
import kotlin.reflect.KClass

interface SqlSerializer<ID> {
    val mapper: SqlMapper
    fun createQuery(kclass: KClass<Any>): Query
    fun selectAllQuery(kclass: KClass<Any>): Query
    fun selectOneQuery(kclass: KClass<Any>, id: ID): Query
    fun insertQuery(obj: Any): InsertQuery
    fun updateQuery(obj: Any): Query
}
