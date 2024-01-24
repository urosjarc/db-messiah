package com.urosjarc.dbjesus.impl

import com.urosjarc.dbjesus.DbEngine
import com.urosjarc.dbjesus.domain.InsertQuery
import com.urosjarc.dbjesus.domain.PreparedInsertQuery
import com.urosjarc.dbjesus.domain.PreparedQuery
import com.urosjarc.dbjesus.domain.Query
import com.urosjarc.dbjesus.exceptions.DbEngineException
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import java.sql.*


class DbJesusEngine<ID_TYPE>(config: HikariConfig) : DbEngine<ID_TYPE> {

    val dataSource = HikariDataSource(config)

    init {
        if (this.dataSource.isClosed && !this.dataSource.isRunning) {
            throw Exception("Database source is closed or not running!")
        }
    }

    private val conn
        get(): Connection {
            try {
                return this.dataSource.connection
            } catch (e: SQLException) {
                throw DbEngineException(msg = "Could not get connection!", cause = e)
            }
        }

    override fun prepareInsertQuery(query: InsertQuery): PreparedInsertQuery {
        val pQuery = PreparedInsertQuery(preparedStatement = this.conn.prepareStatement(query.sql, Statement.RETURN_GENERATED_KEYS))
        query.encoders.forEachIndexed { i, encoder -> encoder(pQuery.preparedStatement, i) }
        return pQuery
    }

    override fun prepareQuery(query: Query): PreparedQuery {
        val pQuery = PreparedQuery(preparedStatement = this.conn.prepareStatement(query.sql))
        query.encoders.forEachIndexed { i, encoder -> encoder(pQuery.preparedStatement, i) }
        return pQuery
    }

    override fun <T> executeQuery(pQuery: PreparedQuery, decodeResultSet: (rs: ResultSet) -> T): List<T> {
        val pstmnt = pQuery.preparedStatement
        val objs = mutableListOf<T>()
        try {
            val rs = pstmnt.executeQuery()
            while (rs.next()) {
                objs.add(decodeResultSet(rs))
            }
        } catch (e: SQLException) {
            throw DbEngineException(msg = "Could not execute select statement!", cause = e)
        }
        return objs
    }

    override fun executeUpdate(pQuery: PreparedQuery): Int {
        val pstmnt = pQuery.preparedStatement
        try {
            return pstmnt.executeUpdate()
        } catch (e: SQLException) {
            throw DbEngineException(msg = "Could not execute update statement!", cause = e)
        }
    }

    override fun executeInsert(pQuery: PreparedInsertQuery, decodeIdResultSet: ((rs: ResultSet, i: Int) -> ID_TYPE)): List<ID_TYPE> {
        val pstmnt = pQuery.preparedStatement
        val ids = mutableListOf<ID_TYPE>()
        try {
            val numUpdates = pstmnt.executeUpdate()
            if (numUpdates == 0) return ids
            val resultSet = pstmnt.generatedKeys
            while (resultSet.next()) {
                ids.add(decodeIdResultSet(resultSet, 1))
            }
        } catch (e: SQLException) {
            throw DbEngineException(msg = "Could not execute insert statement!", cause = e)
        }
        return ids
    }

    override fun executeQueries(pQuery: PreparedQuery, decodeResultSet: (i: Int, rs: ResultSet) -> Unit) {
        val pstmnt = pQuery.preparedStatement
        try {
            var isResultSet = pstmnt.execute()

            var count = 0
            while (true) {
                if (isResultSet) {
                    val rs = pstmnt.resultSet
                    decodeResultSet(count, rs)
                } else if (pstmnt.updateCount == -1) break
                count++
                isResultSet = pstmnt.moreResults
            }

        } catch (e: SQLException) {
            throw DbEngineException(msg = "Could not execute statement!", cause = e)
        }
    }

}
