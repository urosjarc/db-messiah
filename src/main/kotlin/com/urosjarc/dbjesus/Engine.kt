package com.urosjarc.dbjesus

import com.urosjarc.dbjesus.domain.InsertQuery
import com.urosjarc.dbjesus.domain.PreparedInsertQuery
import com.urosjarc.dbjesus.domain.PreparedQuery
import com.urosjarc.dbjesus.domain.Query
import java.sql.ResultSet

interface Engine {
    fun prepareInsertQuery(query: InsertQuery): PreparedInsertQuery
    fun prepareQuery(query: Query): PreparedQuery
    fun <T> executeQuery(pQuery: PreparedQuery, decodeResultSet: (rs: ResultSet) -> T): List<T>
    fun executeUpdate(pQuery: PreparedQuery): Int
    fun <T> executeInsert(pQuery: PreparedInsertQuery, decodeIdResultSet: ((rs: ResultSet, i: Int) -> T)): List<T>
    fun executeQueries(pQuery: PreparedQuery, decodeResultSet: (i: Int, rs: ResultSet) -> Unit)
}
