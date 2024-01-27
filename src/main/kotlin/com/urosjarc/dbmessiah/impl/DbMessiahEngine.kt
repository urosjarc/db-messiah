package com.urosjarc.dbmessiah.impl

import com.urosjarc.dbmessiah.Engine
import com.urosjarc.dbmessiah.domain.queries.*
import com.urosjarc.dbmessiah.domain.serialization.Encoder
import com.urosjarc.dbmessiah.exceptions.EngineException
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.apache.logging.log4j.kotlin.logger
import java.sql.*


class DbMessiahEngine(config: HikariConfig) : Engine {

    val dataSource = HikariDataSource(config)
    val log = this.logger()

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
        this.log.info("Preparing insert query: \n\n${query}")
        val ps = this.conn.prepareStatement(query.sql)
        val pQuery = PreparedInsertQuery(query = query, ps = this.conn.prepareStatement(query.sql, Statement.RETURN_GENERATED_KEYS))
        this.setPreparedStatement(query = query, ps = ps)
        return pQuery
    }

    override fun prepareQuery(query: Query): PreparedQuery {
        this.log.info("Preparing query: \n\n${query}")
        val ps = this.conn.prepareStatement(query.sql)
        val pQuery = PreparedQuery(query = query, ps = ps)
        this.setPreparedStatement(query = query, ps = ps)
        return pQuery
    }

    private fun setPreparedStatement(query: Unsafe, ps: PreparedStatement) {
        //Apply values to prepared statements!!!
        (query.values).forEachIndexed { i, queryValue: QueryValue ->
            if (queryValue.value == null) ps.setNull(i + 1, queryValue.jdbcType.ordinal) //If value is null encoding is done with setNull function !!!
            else (queryValue.encoder as Encoder<Any>)(ps, i + 1, queryValue.value) //If value is not null encoding is done over user defined encoder !!!
        }
        this.log.info("Prepared statement: ${ps}")
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
