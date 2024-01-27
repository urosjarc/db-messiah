package com.urosjarc.dbmessiah

import com.urosjarc.dbmessiah.domain.queries.InsertQuery
import com.urosjarc.dbmessiah.domain.queries.PreparedInsertQuery
import com.urosjarc.dbmessiah.domain.queries.PreparedQuery
import com.urosjarc.dbmessiah.domain.queries.Query
import java.sql.ResultSet

interface Engine {
    fun prepareInsertQuery(query: InsertQuery): PreparedInsertQuery
    fun prepareQuery(query: Query): PreparedQuery
    fun <T> executeQuery(pQuery: PreparedQuery, decodeResultSet: (rs: ResultSet) -> T): List<T>
    fun executeUpdate(pQuery: PreparedQuery): Int
    fun <T> executeInsert(pQuery: PreparedInsertQuery, decodeIdResultSet: ((rs: ResultSet, i: Int) -> T)): List<T>
    fun executeQueries(pQuery: PreparedQuery, decodeResultSet: (i: Int, rs: ResultSet) -> Unit)
}
