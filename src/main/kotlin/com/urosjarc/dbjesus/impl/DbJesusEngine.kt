package com.urosjarc.dbjesus.impl

import com.urosjarc.dbjesus.Engine
import com.urosjarc.dbjesus.domain.queries.*
import com.urosjarc.dbjesus.domain.serialization.Encoder
import com.urosjarc.dbjesus.exceptions.EngineException
import com.urosjarc.dbjesus.exceptions.QueryException
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import java.sql.*


class DbJesusEngine(config: HikariConfig) : Engine {

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
                throw EngineException(msg = "Could not get connection!", cause = e)
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
        //Check query validness!!!
        val sizes = listOf(query.values, query.encoders, query.jdbcTypes)
        if (sizes.toSet().size > 1) throw QueryException("Query does not have equal number of (values, encoders, jdbcTypes): $sizes")

        //Apply values to prepared statements!!!
        (query.encoders).forEachIndexed { i, encoder ->
            encoder as Encoder<Any> // Encoder is acctualy any value that accept only real values
            val value = query.values[i]
            val jdbcType = query.jdbcTypes[i]
            if (value == null) ps.setNull(i, jdbcType.ordinal) //If value is null encoding is done with setNull function !!!
            else encoder(ps, i, value) //If value is not null encoding is done over user defined encoder !!!
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
            throw EngineException(msg = "Could not execute select statement!", cause = e)
        }
        return objs
    }

    override fun executeUpdate(pQuery: PreparedQuery): Int {
        try {
            return pQuery.ps.executeUpdate()
        } catch (e: SQLException) {
            throw EngineException(msg = "Could not execute update statement!", cause = e)
        }
    }

    override fun <T> executeInsert(pQuery: PreparedInsertQuery, decodeIdResultSet: ((rs: ResultSet, i: Int) -> T)): List<T> {
        val pstmnt = pQuery.ps
        val ids = mutableListOf<T>()
        try {
            val numUpdates = pstmnt.executeUpdate()
            if (numUpdates == 0) return ids
            val resultSet = pstmnt.generatedKeys
            while (resultSet.next()) {
                ids.add(decodeIdResultSet(resultSet, 1))
            }
        } catch (e: SQLException) {
            throw EngineException(msg = "Could not execute insert statement!", cause = e)
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
            throw EngineException(msg = "Could not execute statement!", cause = e)
        }
    }

}
