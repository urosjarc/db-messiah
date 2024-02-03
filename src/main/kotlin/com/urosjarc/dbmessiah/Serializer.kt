package com.urosjarc.dbmessiah

import com.urosjarc.dbmessiah.domain.queries.Page
import com.urosjarc.dbmessiah.domain.queries.Query
import com.urosjarc.dbmessiah.domain.queries.QueryBuilder
import com.urosjarc.dbmessiah.domain.serialization.TypeSerializer
import kotlin.reflect.KClass

abstract class Serializer(
    schemas: List<Schema>,
    globalSerializers: List<TypeSerializer<*>>,
    globalInputs: List<KClass<*>>,
    globalOutputs: List<KClass<*>>,
    globalProcedures: List<KClass<*>> = listOf()
) {

    val mapper = Mapper(
        schemas = schemas.toList(),
        globalSerializers = globalSerializers,
        globalInputs = globalInputs,
        globalOutputs = globalOutputs,
        globalProcedures = globalProcedures
    )

    /**
     * MANAGING TABLES
     */

    open val selectLastId: String? = null


    fun <T : Any> dropQuery(kclass: KClass<T>, cascade: Boolean = false): Query {
        val T = this.mapper.getTableInfo(kclass = kclass)
        val cascadeSql = if(cascade) " CASCADE" else ""
        return Query(sql = "DROP TABLE IF EXISTS ${T.path}$cascadeSql;")
    }

    abstract fun <T : Any> createQuery(kclass: KClass<T>): Query
    fun <T : Any> deleteQuery(kclass: KClass<T>, cascade: Boolean = false): Query {
        val T = this.mapper.getTableInfo(kclass = kclass)
        val cascadeSql = if(cascade) " CASCADE" else ""
        return Query(sql = "DELETE FROM ${T.path}$cascadeSql;")
    }

    /**
     * MANAGING ROWS
     */
    fun deleteQuery(obj: Any, cascade: Boolean = false): Query {
        val T = this.mapper.getTableInfo(obj = obj)
        val cascadeSql = if(cascade) " CASCADE" else ""
        return Query(
            sql = "DELETE FROM ${T.path} WHERE ${T.primaryKey.path} = ?$cascadeSql;",
            T.primaryKey.queryValue(obj)
        )
    }

    open fun insertQuery(obj: Any, batch: Boolean): Query {
        val T = this.mapper.getTableInfo(obj = obj)
        return Query(
            sql = "INSERT INTO ${T.path} (${T.sqlInsertColumns()}) VALUES (${T.sqlInsertQuestions()});",
            *T.queryValues(obj = obj),
        )
    }

    fun updateQuery(obj: Any): Query {
        val T = this.mapper.getTableInfo(obj = obj)
        return Query(
            sql = "UPDATE ${T.path} SET ${T.sqlUpdateColumns()} WHERE ${T.primaryKey.path} = ?;",
            *T.queryValues(obj = obj),
            T.primaryKey.queryValue(obj = obj)
        )
    }

    /**
     * SELECTS
     */
    fun <T : Any> query(kclass: KClass<T>): Query {
        val T = this.mapper.getTableInfo(kclass = kclass)
        return Query(sql = "SELECT * FROM ${T.path}")
    }

    open fun <T : Any> query(kclass: KClass<T>, page: Page<T>): Query {
        val T = this.mapper.getTableInfo(kclass = kclass)
        return Query(sql = "SELECT * FROM ${T.path} ORDER BY ${page.orderBy.name} ASC LIMIT ${page.limit} OFFSET ${page.offset}")
    }

    fun <T : Any, K : Any> query(kclass: KClass<T>, pk: K): Query {
        val T = this.mapper.getTableInfo(kclass = kclass)
        return Query(sql = "SELECT * FROM ${T.path} WHERE ${T.primaryKey.path}=$pk")
    }

    /**
     * Call procedure
     */
    open fun <T : Any> callQuery(obj: T): Query {
        val P = this.mapper.getProcedure(obj = obj)
        return Query(sql = "CALL ${P.name}(${P.sqlArguments()};")
    }

    /**
     * Generic queries
     */
    fun query(getSql: () -> String): Query = Query(sql = getSql())
    fun <IN : Any> query(input: IN, getSql: (queryBuilder: QueryBuilder<IN>) -> String): Query {
        val qBuilder = QueryBuilder(mapper = this.mapper, input = input)
        return qBuilder.build(sql = getSql(qBuilder))
    }

}
