package com.urosjarc.dbmessiah.impl.postgresql

import com.urosjarc.dbmessiah.Service
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import com.zaxxer.hikari.util.IsolationLevel
import org.apache.logging.log4j.kotlin.logger
import java.sql.Connection

class PgService(conf: HikariConfig, val ser: PgSerializer) {

    private val log = this.logger()
    private val db = HikariDataSource(conf)
    private val service = Service(conf = conf)
    private fun close(conn: Connection?) = this.service.close(conn = conn)
    private fun rollback(conn: Connection?) = this.service.rollback(conn = conn)
    fun query(readOnly: Boolean = false, body: (conn: PgQueryConn) -> Unit) =
        this.service.query(readOnly = readOnly) { body(PgQueryConn(conn = it, ser = this.ser)) }

    fun transaction(isolationLevel: IsolationLevel? = null, body: (tr: PgTransConn) -> Unit) =
        this.service.transaction(isoLevel = isolationLevel) { body(PgTransConn(conn = it, ser = this.ser)) }
}
