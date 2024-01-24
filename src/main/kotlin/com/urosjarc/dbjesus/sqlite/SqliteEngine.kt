package com.urosjarc.dbjesus.sqlite

import com.urosjarc.dbjesus.SqlEngine
import com.urosjarc.dbjesus.domain.*
import com.urosjarc.dbjesus.exceptions.SqlEngineException
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.apache.logging.log4j.kotlin.logger
import java.sql.*


/**
 *     ResultSet executeQuery(String sql) throws SQLException
 *     int executeUpdate(String sql) throws SQLException
 *     boolean execute(String sql) throws SQLException
 */
class SqliteEngine(config: HikariConfig): SqlEngine {

    val log = this.logger()
    val dataSource = HikariDataSource(config)

    init {
        if (this.dataSource.isClosed && !this.dataSource.isRunning) {
            throw Exception("Database source is closed or not running!")
        }
    }

    private val conn
        get(): Connection? {
            try {
                return this.dataSource.connection
            } catch (e: SQLException) {
                throw SqlEngineException(msg = "Could not get connection!", cause = e)
            }
        }

    override fun prepareInsertQuery(sqlQuery: InsertQuery): PreparedInsertQuery {
        val preparedInsertQuery = PreparedInsertQuery(preparedStatement = this.conn!!.prepareStatement(sqlQuery.sql, Statement.RETURN_GENERATED_KEYS))
        sqlQuery.preparingFuns.forEach { it(preparedInsertQuery.preparedStatement) }
        return preparedInsertQuery
    }

    override fun prepareQuery(query: Query): PreparedQuery {
        val preparedQuery = PreparedQuery(preparedStatement = this.conn!!.prepareStatement(query.sql))
        query.encoders.forEach { it(preparedQuery.preparedStatement) }
        return preparedQuery
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
            throw SqlEngineException(msg = "Could not execute select statement!", cause = e)
        }
        return objs
    }

    override fun executeUpdate(pQuery: PreparedQuery): Int {
        val pstmnt = pQuery.preparedStatement
        try {
            return pstmnt.executeUpdate()
        } catch (e: SQLException) {
            throw SqlEngineException(msg = "Could not execute update statement!", cause = e)
        }
    }

    override fun executeUpdate(pQuery: PreparedInsertQuery): List<Long> {
        val pstmnt = pQuery.preparedStatement
        val ids = mutableListOf<Long>()
        try {
            val numUpdates = pstmnt.executeUpdate()
            if (numUpdates == 0) return ids
            val resultSet = pstmnt.generatedKeys
            while (resultSet.next()) {
                ids.add(resultSet.getLong(1))
            }
        } catch (e: SQLException) {
            throw SqlEngineException(msg = "Could not execute insert statement!", cause = e)
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
            throw SqlEngineException(msg = "Could not execute statement!", cause = e)
        }
    }

}
