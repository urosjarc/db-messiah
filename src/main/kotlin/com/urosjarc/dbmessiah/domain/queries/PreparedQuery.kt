package com.urosjarc.dbmessiah.domain.queries

import com.urosjarc.dbmessiah.exceptions.EngineException
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet

class PreparedQuery(val query: Query, val ps: PreparedStatement, val conn: Connection){
    fun close(rs: ResultSet? = null){
        try { rs?.close() } catch (e: Throwable) { throw EngineException("Could not close resultSet for query: ${this.query}")}
        try { this.ps.close() } catch (e: Throwable) { throw EngineException("Could not close prepared statement for query: ${this.query}")}
        try { this.conn.close() } catch (e: Throwable) { throw EngineException("Could not close connection for query: ${this.query}")}
    }

    fun <T: Any?> closeAndReturn(rs: ResultSet? = null, data: T?): T? {
        this.close(rs = rs)
        return data
    }
}
