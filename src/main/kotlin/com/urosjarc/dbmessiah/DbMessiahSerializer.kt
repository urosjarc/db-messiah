package com.urosjarc.dbmessiah

import com.urosjarc.dbmessiah.domain.queries.Page
import com.urosjarc.dbmessiah.domain.queries.Query
import com.urosjarc.dbmessiah.domain.queries.QueryBuilderInOut
import com.urosjarc.dbmessiah.domain.queries.QueryBuilderOut
import com.urosjarc.dbmessiah.domain.schema.Schema
import com.urosjarc.dbmessiah.domain.serialization.TypeSerializer
import kotlin.reflect.KClass

interface DbMessiahSerializer {
    val testCRUD: Boolean
    val repo: DbMessiahRepository
    val schemas: List<Schema>
    val globalSerializers: List<TypeSerializer<*>>
    val globalInputs: List<KClass<*>>
    val globalOutputs: List<KClass<*>>

    /**
     * MANAGING TABLES
     */

    val onGeneratedKeysFail: String

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
    fun <OUT : Any> selectQuery(output: KClass<OUT>, getSql: (queryBuilder: QueryBuilderOut<Unit, OUT>) -> String): Query {
        val queryBuilder = QueryBuilderOut(input = Unit, output = output, mapper = this.repo)
        return queryBuilder.build(sql = getSql(queryBuilder))
    }

    fun <IN : Any, OUT : Any> selectQuery(input: IN, output: KClass<OUT>, getSql: (queryBuilder: QueryBuilderInOut<IN, OUT>) -> String): Query {
        val queryBuilder = QueryBuilderInOut(input = input, output = output, mapper = this.repo)
        return queryBuilder.build(sql = getSql(queryBuilder))
    }

}
