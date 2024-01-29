package com.urosjarc.dbmessiah

import com.urosjarc.dbmessiah.domain.queries.BatchQuery
import com.urosjarc.dbmessiah.domain.queries.Query
import java.sql.ResultSet
import kotlin.reflect.KProperty1

interface Engine {
    fun <T> executeQuery(query: Query, decodeResultSet: (rs: ResultSet) -> T): List<T>
    fun executeUpdate(query: Query): Int
    fun <T> executeInsert(query: Query, primaryKey: KProperty1<T, *>, onGeneratedKeysFail: String? = null, decodeIdResultSet: ((rs: ResultSet, i: Int) -> T)): T?
    fun executeBatch(batchQuery: BatchQuery): Int
    fun executeQueries(query: Query, decodeResultSet: (i: Int, rs: ResultSet) -> Unit)
}
