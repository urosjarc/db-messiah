package com.urosjarc.diysqlservice

import com.urosjarc.diysqlservice.domain.SqlMapper
import kotlin.reflect.KClass

interface SqlGenerator {
    val sqlMapper: SqlMapper
    fun <T : Any> createTable(kclass: KClass<T>): String
    fun <T : Any> selectTable(cls: KClass<T>): String
    fun insertTable(obj: Any): String
    fun updateTable(obj: Any): String
}
