package com.urosjarc.dbmessiah

import com.urosjarc.dbmessiah.domain.queries.BatchQuery
import com.urosjarc.dbmessiah.domain.queries.Query
import com.urosjarc.dbmessiah.domain.queries.QueryValue
import com.urosjarc.dbmessiah.domain.serialization.Encoder
import com.urosjarc.dbmessiah.exceptions.EngineException
import com.urosjarc.dbmessiah.exceptions.base.ReportIssue
import org.apache.logging.log4j.kotlin.logger
import java.sql.*

open class Engine(private val conn: Connection) {
    private val log = this.logger()

    private fun prepareQuery(ps: PreparedStatement, query: Query) {
        var rawSql = query.sql
        //Apply values to prepared statement
        (query.values).forEachIndexed { i, queryValue: QueryValue ->
            rawSql = rawSql.replaceFirst("?", queryValue.escapped)
            if (queryValue.value == null) ps.setNull(i + 1, queryValue.jdbcType.ordinal) //If value is null encoding is done with setNull function !!!
            else (queryValue.encoder as Encoder<Any>)(ps, i + 1, queryValue.value) //If value is not null encoding is done over user defined encoder !!!
        }
        this.log.info("Prepare query: $rawSql")
    }

    private fun closeAll(ps: PreparedStatement? = null, rs: ResultSet? = null) {
        try {
            ps?.close()
        } catch (e: Throwable) {
            this.log.error("Closing prepared statement failed: ${e.message}", e)
        }
        try {
            rs?.close()
        } catch (e: Throwable) {
            this.log.error("Closing resultSet failed: ${e.message}", e)
        }
    }

    fun batch(batchQuery: BatchQuery): Int {
        var ps: PreparedStatement? = null
        var numUpdates = 0

        try {
            //Prepare statement and query
            ps = conn.prepareStatement(batchQuery.sql)

            var i = 0
            for (values in batchQuery.valueMatrix) {
                this.prepareQuery(ps = ps, query = Query(sql = batchQuery.sql, values = values.toTypedArray()))
                ps.addBatch()
                if (++i % 1_000 == 0) {
                    val exeCount = ps.executeBatch()
                    numUpdates += exeCount.sum()
                    ps.clearParameters()
                    i = 0
                }
            }
            if (i > 0) {
                val exeCount = ps.executeBatch()
                numUpdates += exeCount.sum()
            }

            //Close everything
            this.closeAll(ps = ps)

            //Return result
            return numUpdates

        } catch (e: SQLException) {
            this.closeAll(ps = ps)
            throw EngineException(msg = "Could not batch query statement: ${e.message}", cause = e)
        } catch (e: Throwable) {
            this.closeAll(ps = ps)
            throw ReportIssue(msg = "Unknown executor exception: ${e.message}", cause = e)
        }
    }

    fun update(query: Query): Int {
        var ps: PreparedStatement? = null

        try {
            //Prepare statement
            ps = conn.prepareStatement(query.sql)
            this.prepareQuery(ps = ps, query = query)

            //Get info
            val count: Int = ps.executeUpdate()

            //Close all
            this.closeAll(ps = ps)

            //Return count
            return count
        } catch (e: SQLException) {
            this.closeAll(ps = ps)
            throw EngineException(msg = "Could not execute update statement: ${e.message}", cause = e)
        } catch (e: Throwable) {
            this.closeAll(ps = ps)
            throw ReportIssue(msg = "Unknown executor exception: ${e.message}", cause = e)
        }
    }

    fun <T> insert(query: Query, onGeneratedKeysFail: String? = null, decodeIdResultSet: ((rs: ResultSet, i: Int) -> T)): T? {
        var ps: PreparedStatement? = null
        var rs: ResultSet? = null
        var rs2: ResultSet? = null

        //Execute query
        try {
            //Prepare statement
            ps = conn.prepareStatement(query.sql, Statement.RETURN_GENERATED_KEYS)
            this.prepareQuery(ps = ps, query = query)

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
            throw EngineException(msg = "Could not execute insert statement: ${e.message}", cause = e)
        } catch (e: Throwable) {
            this.closeAll(ps = ps)
            throw ReportIssue(msg = "Unknown executor exception: ${e.message}", cause = e)
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
        } catch (e: Throwable) {
            this.closeAll(ps = ps, rs = rs)
            throw ReportIssue(msg = "Unknown executor exception: ${e.message}", cause = e)
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
                throw EngineException(msg = "Could not execute on generated keys fail sql: '$onGeneratedKeysFail'", cause = e)
            } catch (e: Throwable) {
                this.closeAll(ps = ps, rs = rs2)
                throw ReportIssue(msg = "Unknown executor exception: ${e.message}", cause = e)
            } finally {
                this.closeAll(ps = ps, rs = rs2)
            }
        }

        throw ReportIssue(msg = "Could not retrieve inserted id normally nor with force!")
    }

    fun execute(query: Query, decodeResultSet: (i: Int, rs: ResultSet) -> List<Any>?): MutableList<List<Any>?> {
        var ps: PreparedStatement? = null
        var rs: ResultSet? = null
        val returned = mutableListOf<List<Any>?>()

        try {
            ps = conn.prepareStatement(query.sql)
            this.prepareQuery(ps = ps, query = query)
            var isResultSet = ps.execute()

            var count = 0
            while (true) {
                if (isResultSet) {
                    rs = ps.resultSet
                    returned.add(decodeResultSet(count, rs))
                    rs.close()
                } else if (ps.updateCount == -1) break //If there is no more result finish

                count++
                isResultSet = ps.moreResults //Get next result
            }

            return returned
        } catch (e: SQLException) {
            this.closeAll(ps = ps, rs = rs)
            throw EngineException(msg = "Could not execute statement: ${e.message}", cause = e)
        } catch (e: Throwable) {
            this.closeAll(ps = ps, rs = rs)
            throw ReportIssue(msg = "Unknown executor exception: ${e.message}", cause = e)
        }
    }

    fun <T> query(query: Query, decodeResultSet: (rs: ResultSet) -> T): List<T> {
        var ps: PreparedStatement? = null
        var rs: ResultSet? = null

        try {
            //Prepare statement and query
            ps = conn.prepareStatement(query.sql)
            this.prepareQuery(ps = ps, query = query)

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
            throw EngineException(msg = "Could not execute select statement: ${e.message}", cause = e)
        } catch (e: Throwable) {
            this.closeAll(ps = ps, rs = rs)
            throw ReportIssue(msg = "Unknown executor exception: ${e.message}", cause = e)
        }
    }

}
