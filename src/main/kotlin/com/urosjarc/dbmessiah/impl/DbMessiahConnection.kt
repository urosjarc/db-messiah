package com.urosjarc.dbmessiah.impl

import com.urosjarc.dbmessiah.Engine
import com.urosjarc.dbmessiah.domain.engine.ConnectionWrapper
import com.urosjarc.dbmessiah.domain.queries.BatchQuery
import com.urosjarc.dbmessiah.domain.queries.Query
import com.urosjarc.dbmessiah.domain.queries.QueryValue
import com.urosjarc.dbmessiah.domain.serialization.Encoder
import com.urosjarc.dbmessiah.exceptions.EngineException
import org.apache.logging.log4j.kotlin.logger
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException
import kotlin.reflect.KProperty1


class DbMessiahConnection(val conn: Connection) : Engine {

    val log = this.logger()

    init {
        if (this.conn.isClosed) throw Exception("Database connection is closed or not running!")
    }

    private fun prepareQuery(ps: PreparedStatement, queryValues: Array<out QueryValue>) {
        //Apply values to prepared statement
        (queryValues).forEachIndexed { i, queryValue: QueryValue ->
            println(queryValue)
            if (queryValue.value == null) ps.setNull(i + 1, queryValue.jdbcType.ordinal) //If value is null encoding is done with setNull function !!!
            else (queryValue.encoder as Encoder<Any>)(ps, i + 1, queryValue.value) //If value is not null encoding is done over user defined encoder !!!
        }
    }

    fun closeAll(conn: Connection? = null, ps: PreparedStatement? = null, rs: ResultSet? = null) {
        try {
            conn?.close()
        } catch (e: Throwable) {
            this.log.error("Closing connection failed", e)
        }
        try {
            ps?.close()
        } catch (e: Throwable) {
            this.log.error("Closing prepared statement failed", e)
        }
        try {
            rs?.close()
        } catch (e: Throwable) {
            this.log.error("Closing resultSet failed", e)
        }
    }

    fun transaction(cb: (connWrapp: ConnectionWrapper) -> Unit) {
        try {
            this.conn.autoCommit = false
            cb(this.conn)
            this.conn.commit()
        } catch (e: Throwable){
            this.log.error("Failed to execute full transaction, collback", e)
            this.conn.rollback()
        }
        this.closeAll(conn = this.conn)
    }

    override fun <T> executeQuery(query: Query, decodeResultSet: (rs: ResultSet) -> T): List<T> {
        var ps: PreparedStatement? = null
        var rs: ResultSet? = null

        try {
            //Prepare statement and query
            val ps = conn.prepareStatement(query.sql)
            this.prepareQuery(ps = ps, queryValues = query.values)

            //Define results
            val objs = mutableListOf<T>()

            //Create result set and fill element
            rs = ps.executeQuery()
            while (rs.next()) {
                objs.add(decodeResultSet(rs))
            }

            //Close everything
            this.closeAll(ps = ps, rs = rs)

            //Return objects
            return objs

        } catch (e: SQLException) {
            this.closeAll(ps = ps, rs = rs)
            throw EngineException(msg = "Could not execute select statement!", cause = e)
        } catch (e: Throwable) {
            this.closeAll(ps = ps, rs = rs)
            throw EngineException(msg = "Unknown exception", cause = e)
        }
    }

    override fun executeBatch(batchQuery: BatchQuery): Int {
        var ps: PreparedStatement? = null
        var numUpdates = 0

        try {
            if (batchQuery.sql.contains("UPDATE")) {
                println(batchQuery.sql)

            }
            //Prepare statement and query
            val ps = conn.prepareStatement(batchQuery.sql)

            var i = 0
            for (values in batchQuery.valueMatrix) {
                this.prepareQuery(ps = ps, queryValues = values.toTypedArray())
                ps.addBatch()
                if (++i % 1_000 == 0) {
                    numUpdates += ps.executeBatch().sum()
                    ps.clearParameters()
                    i = 0
                }
            }
            if (i > 0) numUpdates += ps.executeBatch().sum()

            //Close everything
            this.closeAll(ps = ps)

            //Return result
            return numUpdates

        } catch (e: SQLException) {
            this.closeAll(ps = ps)
            throw EngineException(msg = "Could not batch query statement!", cause = e)
        } catch (e: Throwable) {
            this.closeAll(ps = ps)
            throw EngineException(msg = "Unknown exception", cause = e)
        }
    }

    override fun executeUpdate(query: Query): Int {
        var ps: PreparedStatement? = null

        try {
            //Prepare statement
            ps = conn.prepareStatement(query.sql)
            this.prepareQuery(ps = ps, queryValues = query.values)

            //Get info
            val count: Int = ps.executeUpdate()

            //Close all
            this.closeAll(ps = ps)

            //Return count
            return count
        } catch (e: SQLException) {
            this.closeAll(ps = ps)
            throw EngineException(msg = "Could not execute update statement!", cause = e)
        } catch (e: Throwable) {
            this.closeAll(ps = ps)
            throw EngineException(msg = "Unknown exception", cause = e)
        }
    }

    override fun <T> executeInsert(query: Query, primaryKey: KProperty1<T, *>, onGeneratedKeysFail: String?, decodeIdResultSet: ((rs: ResultSet, i: Int) -> T)): T? {
        var ps: PreparedStatement? = null
        var rs: ResultSet? = null
        var rs2: ResultSet? = null

        //Execute query
        try {
            //Prepare statement
            ps = conn.prepareStatement(query.sql)
            this.prepareQuery(ps = ps, queryValues = query.values)

            //Get info
            val numUpdates = ps.executeUpdate()

            //If no updates happend close all
            if (numUpdates == 0) {
                this.closeAll(ps = ps)
                return null
            }
            //Continue with getting ids for inserts
        } catch (e: SQLException) {
            this.closeAll(ps = ps)
            throw EngineException(msg = "Could not execute insert statement!", cause = e)
        } catch (e: Throwable) {
            this.closeAll(ps = ps)
            throw EngineException(msg = "Unknown exception", cause = e)
        }

        //Try fetching ids normaly
        try {
            rs = ps.generatedKeys
            if (rs.next()) {
                val data = decodeIdResultSet(rs, 1)
                this.closeAll(ps = ps, rs = rs)
                return data
            }
        } catch (e: SQLException) {
            //Auto reurn id is probably not supported
            //Continue with execution
            rs?.close()
            this.log.warn(e)
        } catch (e: Throwable) {
            this.closeAll(ps = ps, rs = rs)
            throw EngineException(msg = "Unknown exception", cause = e)
        }

        //Try fetching ids with force
        if (onGeneratedKeysFail != null) {
            try {
                rs2 = ps.connection.prepareStatement(onGeneratedKeysFail).executeQuery()
                if (rs2.next()) {
                    val data = decodeIdResultSet(rs2, 1)
                    this.closeAll(ps = ps, rs = rs2)
                    return data
                }
            } catch (e: SQLException) {
                this.closeAll(ps = ps, rs = rs2)
                throw EngineException(msg = "Could not execute on generated keys fail sql", cause = e)
            } catch (e: Throwable) {
                this.closeAll(ps = ps, rs = rs2)
                throw EngineException(msg = "Unknown fatal error occurred, please report this issue!", cause = e)
            } finally {
                this.closeAll(ps = ps, rs = rs2)
            }
        }

        throw EngineException(msg = "Could not retrieve inserted id normally nor with force!")
    }

    override fun executeQueries(query: Query, decodeResultSet: (i: Int, rs: ResultSet) -> Unit) {
        var ps: PreparedStatement? = null
        var rs: ResultSet? = null

        try {
            ps = conn.prepareStatement(query.sql)
            this.prepareQuery(ps = ps, queryValues = query.values)
            var isResultSet = ps.execute()

            var count = 0
            while (true) {
                if (isResultSet) {
                    rs = ps.resultSet
                    decodeResultSet(count, rs)
                    rs.close()
                } else if (ps.updateCount == -1) break
                count++
                isResultSet = ps.moreResults
            }

        } catch (e: SQLException) {
            this.closeAll(ps = ps, rs = rs)
            throw EngineException(msg = "Could not execute statement!", cause = e)
        } catch (e: Throwable) {
            this.closeAll(ps = ps, rs = rs)
            throw EngineException(msg = "Unknown fatal error occurred, please report this issue!", cause = e)
        }
    }

}
