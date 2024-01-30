package com.urosjarc.dbmessiah.impl

import com.urosjarc.dbmessiah.Serializer
import com.urosjarc.dbmessiah.exceptions.ServiceException
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import com.zaxxer.hikari.util.IsolationLevel
import org.apache.logging.log4j.kotlin.logger
import java.sql.Connection

class DbService(
    val config: HikariConfig,
    val serializer: Serializer,
) {

    val log = this.logger()
    val db = HikariDataSource(config)

    private fun close(conn: Connection?) {
        try {
            conn?.close()
        } catch (e: Throwable) {
            this.log.error("Closing connection failed", e)
        }
    }

    private fun rollback(conn: Connection?) {
        try {
            conn?.rollback()
        } catch (e: Throwable) {
            this.log.error("Rollback connection failed", e)
        }
    }

    fun exe(readOnly: Boolean = false, body: (conn: DbConnection) -> Unit) {
        var conn: Connection? = null
        try {
            conn = db.connection
            val connWrapper = DbConnection(conn = conn, ser = serializer)
            body(connWrapper)

            this.close(conn = conn)
        } catch (e: Throwable) {
            this.close(conn = conn)
            throw ServiceException("Unknown execution error", e)
        }
    }

    fun transaction(isolationLevel: IsolationLevel? = null, body: (tr: DbTransaction) -> Boolean?) {
        var conn: Connection? = null
        try {
            //Getting connection
            conn = db.connection

            //Start transaction
            conn.autoCommit = false

            //Set user defined isolation level
            if (isolationLevel != null) conn.transactionIsolation = isolationLevel.ordinal

            //Execute transaction body and get user result if service can commit changes
            val tr = DbTransaction(conn = conn, ser = serializer)
            val canCommit = body(tr)

            //If user returns nothing or true you can commit otherwise rollback to previous state.
            if (canCommit != false) conn.commit()
            else conn.rollback()

            //Finaly close the connection
            conn.close()
        } catch (e: Throwable) {
            //If any error occurse that is not user handled then rollback, close and raise exception
            this.rollback(conn = conn)
            this.close(conn = conn)
            throw ServiceException("Unknown transaction error", e)
        }
    }
}
