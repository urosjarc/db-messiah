package com.urosjarc.dbmessiah.impl.db2

import com.urosjarc.dbmessiah.Service
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import com.zaxxer.hikari.util.IsolationLevel
import org.apache.logging.log4j.kotlin.logger
import java.sql.Connection

class Db2Service(conf: HikariConfig, val ser: OracleSerializer) {

    private val log = this.logger()
    private val db = HikariDataSource(conf)
    private val service = Service(conf = conf)
    private fun close(conn: Connection?) = this.service.close(conn = conn)
    private fun rollback(conn: Connection?) = this.service.rollback(conn = conn)
    fun query(readOnly: Boolean = false, body: (conn: OracleQueryConn) -> Unit) =
        this.service.query(readOnly = readOnly) { body(OracleQueryConn(conn = it, ser = this.ser)) }

    fun transaction(isolationLevel: IsolationLevel? = null, body: (tr: Db2TransConn) -> Unit) =
        this.service.transaction(isoLevel = isolationLevel) { body(Db2TransConn(conn = it, ser = this.ser)) }
}
