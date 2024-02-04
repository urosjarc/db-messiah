package com.urosjarc.dbmessiah.impl.mariadb

import com.urosjarc.dbmessiah.Service
import com.urosjarc.dbmessiah.impl.mssql.MssqlQueryConn
import com.urosjarc.dbmessiah.impl.mssql.MssqlSerializer
import com.urosjarc.dbmessiah.impl.mssql.MssqlTransConn
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import com.zaxxer.hikari.util.IsolationLevel
import org.apache.logging.log4j.kotlin.logger
import java.sql.Connection

class MariaService(conf: HikariConfig, val ser: MariaSerializer) {

    private val log = this.logger()
    private val db = HikariDataSource(conf)
    private val service = Service(conf = conf)
    private fun close(conn: Connection?) = this.service.close(conn = conn)
    private fun rollback(conn: Connection?) = this.service.rollback(conn = conn)
    fun query(readOnly: Boolean = false, body: (conn: MariaQueryConn) -> Unit) =
        this.service.query(readOnly = readOnly) { body(MariaQueryConn(conn = it, ser = this.ser)) }

    fun transaction(isolationLevel: IsolationLevel? = null, body: (tr: MariaTransConn) -> Unit) =
        this.service.transaction(isoLevel = isolationLevel) { body(MariaTransConn(conn = it, ser = this.ser)) }
}
