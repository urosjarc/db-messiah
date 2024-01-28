package com.urosjarc.dbmessiah

import com.urosjarc.dbmessiah.domain.queries.Page
import com.urosjarc.dbmessiah.domain.queries.Query
import com.urosjarc.dbmessiah.domain.queries.QueryBuilder
import com.urosjarc.dbmessiah.domain.schema.Schema
import com.urosjarc.dbmessiah.domain.serialization.TypeSerializer
import java.sql.ResultSet
import kotlin.reflect.KClass

interface Serializer {

    val mapper: Mapper
    val schemas: List<Schema>
    val globalSerializers: List<TypeSerializer<*>>

    /**
     * MANAGING TABLES
     */

    fun <T: Any> dropQuery(kclass: KClass<T>): Query
    fun <T : Any> createQuery(kclass: KClass<T>): Query

    /**
     * MANAGING ROWS
     */
    fun insertQuery(obj: Any): Query
    fun updateQuery(obj: Any): Query
    fun deleteQuery(obj: Any): Query

    /**
     * SELECTS
     */
    fun <T : Any> selectAllQuery(kclass: KClass<T>): Query
    fun <T : Any> selectPageQuery(kclass: KClass<T>, page: Page<T>): Query
    fun <T : Any, K: Any> selectOneQuery(kclass: KClass<T>, pkValue: K): Query
}
