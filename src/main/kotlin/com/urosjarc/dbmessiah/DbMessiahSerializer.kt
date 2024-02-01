package com.urosjarc.dbmessiah

import com.urosjarc.dbmessiah.domain.queries.*
import com.urosjarc.dbmessiah.domain.schema.Schema
import com.urosjarc.dbmessiah.domain.serialization.TypeSerializer
import com.urosjarc.dbmessiah.domain.table.Escaper
import kotlin.reflect.KClass

abstract class DbMessiahSerializer(
    escaper: Escaper,
    schemas: List<Schema>,
    globalSerializers: List<TypeSerializer<*>> = listOf(),
    globalInputs: List<KClass<*>> = listOf(),
    globalOutputs: List<KClass<*>> = listOf(),
    injectTestElements: Boolean = false,
) {

    val mapper = DbMessiahMapper(
        escaper = escaper,
        injectTestElements = injectTestElements,
        schemas = schemas.toList(),
        globalSerializers = globalSerializers,
        globalInputs = globalInputs,
        globalOutputs = globalOutputs
    )

    /**
     * MANAGING TABLES
     */

    abstract val onGeneratedKeysFail: String

    abstract fun <T : Any> dropQuery(kclass: KClass<T>): Query
    abstract fun <T : Any> createQuery(kclass: KClass<T>): Query
    abstract fun <T : Any> deleteQuery(kclass: KClass<T>): Query

    /**
     * MANAGING ROWS
     */
    abstract fun insertQuery(obj: Any): Query
    abstract fun updateQuery(obj: Any): Query
    abstract fun deleteQuery(obj: Any): Query

    /**
     * SELECTS
     */
    abstract fun <T : Any> selectQuery(kclass: KClass<T>): Query
    abstract fun <T : Any> selectQuery(kclass: KClass<T>, page: Page<T>): Query
    abstract fun <T : Any, K : Any> selectQuery(kclass: KClass<T>, pk: K): Query

    /**
     * Call procedure
     */
    fun <IN : Any> callQuery(input: IN): Query {
        val T = this.mapper.getTableInfo(obj = input)
        return Query(
            sql = "{CALL ${input::class.simpleName}(${T.sqlInsertQuestions()})}",
            *T.queryValues(obj = input)
        )
    }

    /**
     * Generic queries
     */
    fun selectQuery(getSql: (queryBuilder: QueryBuilder) -> String): Query {
        val queryBuilder = QueryBuilder(mapper = this.mapper)
        return queryBuilder.build(sql = getSql(queryBuilder))
    }
    fun <OUT : Any> selectQuery(output: KClass<OUT>, getSql: (queryBuilder: QueryBuilderOut<OUT>) -> String): Query {
        val queryBuilder = QueryBuilderOut(output = output, mapper = this.mapper)
        return queryBuilder.build(sql = getSql(queryBuilder))
    }

    fun <IN : Any, OUT : Any> selectQuery(input: IN, output: KClass<OUT>, getSql: (queryBuilder: QueryBuilderInOut<IN, OUT>) -> String): Query {
        val queryBuilder = QueryBuilderInOut(input = input, output = output, mapper = this.mapper)
        return queryBuilder.build(sql = getSql(queryBuilder))
    }

}
