package com.urosjarc.dbjesus.impl

import com.urosjarc.dbjesus.DbEngine
import com.urosjarc.dbjesus.domain.*
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
        val ps = this.conn.prepareStatement(query.sql)
        val pQuery = PreparedInsertQuery(query = query, ps = this.conn.prepareStatement(query.sql, Statement.RETURN_GENERATED_KEYS))
        this.setPreparedStatement(query = query, ps = ps)
        return pQuery
    }

    override fun prepareQuery(query: Query): PreparedQuery {
        val ps = this.conn.prepareStatement(query.sql)
        val pQuery = PreparedQuery(query = query, ps = ps)
        this.setPreparedStatement(query = query, ps = ps)
        return pQuery
    }

    private fun setPreparedStatement(query: Unsafe, ps: PreparedStatement) {
        query.encoders.forEachIndexed { i, encoder ->
            val value = query.values[i]
            val jdbcType = query.jdbcTypes[i]
            if (value == null) ps.setNull(i, jdbcType.ordinal)
            else encoder(ps, i, value)
        }
    }

    override fun <T> executeQuery(pQuery: PreparedQuery, decodeResultSet: (rs: ResultSet) -> T): List<T> {
        val objs = mutableListOf<T>()
        try {
            val rs = pQuery.ps.executeQuery()
            while (rs.next()) {
                objs.add(decodeResultSet(rs))
            }
        } catch (e: SQLException) {
            throw DbEngineException(msg = "Could not execute select statement!", cause = e)
        }
        return objs
    }

    override fun executeUpdate(pQuery: PreparedQuery): Int {
        try {
            return pQuery.ps.executeUpdate()
        } catch (e: SQLException) {
            throw DbEngineException(msg = "Could not execute update statement!", cause = e)
        }
    }

    override fun executeInsert(pQuery: PreparedInsertQuery, decodeIdResultSet: ((rs: ResultSet, i: Int) -> ID_TYPE)): List<ID_TYPE> {
        val pstmnt = pQuery.ps
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
        val ps = pQuery.ps
        try {
            var isResultSet = ps.execute()

            var count = 0
            while (true) {
                if (isResultSet) {
                    val rs = ps.resultSet
                    decodeResultSet(count, rs)
                } else if (ps.updateCount == -1) break
                count++
                isResultSet = ps.moreResults
            }

        } catch (e: SQLException) {
            throw DbEngineException(msg = "Could not execute statement!", cause = e)
        }
    }

}
