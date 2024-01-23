package com.urosjarc.dbjesus

import com.urosjarc.dbjesus.domain.SqlMapper
import kotlin.reflect.KClass

interface SqlGenerator<ID> {
    val sqlMapper: SqlMapper
    fun <T : Any> createTable(kclass: KClass<T>): String
    fun <T : Any> select(cls: KClass<T>, where: String): String
    fun <T : Any> selectOne(cls: KClass<T>, id: ID): String
    fun insert(obj: Any): String
    fun update(obj: Any): String
}
