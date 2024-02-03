package com.urosjarc.dbmessiah

import com.urosjarc.dbmessiah.exceptions.ServiceException
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import com.zaxxer.hikari.util.IsolationLevel
import org.apache.logging.log4j.kotlin.logger
import java.sql.Connection

class Service(val conf: HikariConfig) {

    val log = this.logger()
    val db = HikariDataSource(conf)

    fun close(conn: Connection?) {
        try {
            conn?.close()
        } catch (e: Throwable) {
            this.log.error("Closing connection failed", e)
        }
    }

    fun rollback(conn: Connection?) {
        try {
            conn?.rollback()
        } catch (e: Throwable) {
            this.log.error("Rollback connection failed", e)
        }
    }

    fun query(readOnly: Boolean = false, body: (conn: Connection) -> Unit) {
        var conn: Connection? = null
        try {
            //Getting connection
            conn = db.connection

            //Will connection be read only
            conn.isReadOnly = readOnly

            //Execute query body and get user result
            body(conn)

            //Close connection
            this.close(conn = conn)

        } catch (e: Throwable) {
            this.close(conn = conn)
            throw ServiceException("Unknown execution error: ${e.message}", e)
        }
    }

    fun transaction(isoLevel: IsolationLevel? = null, body: (conn: Connection) -> Unit) {
        var conn: Connection? = null
        try {
            //Getting connection
            conn = db.connection

            //Start transaction
            conn.autoCommit = false

            //Set user defined isolation level
            this.log.info("Transaction type: ${conn.transactionIsolation}")
            if (isoLevel != null) conn.transactionIsolation = isoLevel.ordinal

            //Execute transaction body and get user result
            body(conn)

            //Commit changes and close connection
            conn.commit()
            conn.close()
        } catch (e: Throwable) {
            //If any error occurse that is not user handled then rollback, close and raise exception
            this.rollback(conn = conn)
            this.close(conn = conn)
            throw ServiceException("Unknown transaction error", e)
        }
    }
}
