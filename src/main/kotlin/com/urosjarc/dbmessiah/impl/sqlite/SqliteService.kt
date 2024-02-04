package com.urosjarc.dbmessiah.impl.sqlite

import com.urosjarc.dbmessiah.Service
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import com.zaxxer.hikari.util.IsolationLevel
import org.apache.logging.log4j.kotlin.logger
import java.sql.Connection

class SqliteService(conf: HikariConfig, val ser: SqliteSerializer) {

    private val log = this.logger()
    private val db = HikariDataSource(conf)
    private val service = Service(conf = conf)
    private fun close(conn: Connection?) = this.service.close(conn = conn)
    private fun rollback(conn: Connection?) = this.service.rollback(conn = conn)
    fun query(readOnly: Boolean = false, body: (conn: SqliteQueryConn) -> Unit) =
        this.service.query(readOnly = readOnly) { body(SqliteQueryConn(conn = it, ser = this.ser)) }

    fun transaction(isolationLevel: IsolationLevel? = null, body: (tr: SqliteTransConn) -> Unit) =
        this.service.transaction(isoLevel = isolationLevel) { body(SqliteTransConn(conn = it, ser = this.ser)) }
}
