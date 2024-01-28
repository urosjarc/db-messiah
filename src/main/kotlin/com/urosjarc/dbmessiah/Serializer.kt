package com.urosjarc.dbmessiah

import com.urosjarc.dbmessiah.domain.queries.Page
import com.urosjarc.dbmessiah.domain.queries.Query
import com.urosjarc.dbmessiah.domain.queries.QueryBuilder
import com.urosjarc.dbmessiah.domain.schema.Schema
import com.urosjarc.dbmessiah.domain.serialization.TypeSerializer
import kotlin.reflect.KClass

interface Serializer {
    val testCRUD: Boolean
    val mapper: Mapper
    val schemas: List<Schema>
    val globalSerializers: List<TypeSerializer<*>>
    val globalInputs: List<KClass<*>>

    /**
     * MANAGING TABLES
     */

    fun <T : Any> dropQuery(kclass: KClass<T>): Query
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
    fun <T : Any> selectQuery(kclass: KClass<T>): Query
    fun <T : Any> selectQuery(kclass: KClass<T>, page: Page<T>): Query
    fun <T : Any, K : Any> selectQuery(kclass: KClass<T>, pk: K): Query

    /**
     * Generic queries
     */
    fun <T : Any> selectQuery(obj: T, getSql: (queryBuilder: QueryBuilder<T>) -> String): Query {
        val queryBuilder = QueryBuilder(sourceObj = obj, mapper = this.mapper)
        return queryBuilder.build(sql = getSql(queryBuilder))
    }

}
