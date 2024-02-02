package com.urosjarc.dbmessiah

import com.urosjarc.dbmessiah.domain.queries.Page
import com.urosjarc.dbmessiah.domain.queries.Query
import com.urosjarc.dbmessiah.domain.queries.QueryBuilder
import com.urosjarc.dbmessiah.domain.schema.Schema
import com.urosjarc.dbmessiah.domain.serialization.TypeSerializer
import com.urosjarc.dbmessiah.domain.table.Escaper
import kotlin.reflect.KClass

abstract class DbMessiahSerializer(
    schemas: List<Schema>,
    globalSerializers: List<TypeSerializer<*>> = listOf(),
    globalInputs: List<KClass<*>> = listOf(),
    globalOutputs: List<KClass<*>> = listOf(),
    globalProcedures: List<KClass<*>> = listOf(),
    escaper: Escaper,
) {

    val mapper = DbMessiahMapper(
        escaper = escaper,
        schemas = schemas.toList(),
        globalSerializers = globalSerializers,
        globalInputs = globalInputs,
        globalOutputs = globalOutputs,
        globalProcedures = globalProcedures
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
    abstract fun <T : Any> query(kclass: KClass<T>): Query
    abstract fun <T : Any> query(kclass: KClass<T>, page: Page<T>): Query
    abstract fun <T : Any, K : Any> query(kclass: KClass<T>, pk: K): Query

    /**
     * Call procedure
     */
    abstract fun <T : Any> callQuery(obj: T): Query

    /**
     * Generic queries
     */
    fun query(getSql: () -> String): Query = Query(sql = getSql())
    fun <IN : Any> query(input: IN, getSql: (queryBuilder: QueryBuilder<IN>) -> String): Query {
        val qBuilder = QueryBuilder(mapper = this.mapper, input = input)
        return qBuilder.build(sql = getSql(qBuilder))
    }

}
