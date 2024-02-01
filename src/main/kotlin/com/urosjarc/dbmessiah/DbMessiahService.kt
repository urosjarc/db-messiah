package com.urosjarc.dbmessiah

import com.urosjarc.dbmessiah.exceptions.ServiceException
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import com.zaxxer.hikari.util.IsolationLevel
import org.apache.logging.log4j.kotlin.logger
import java.sql.Connection

class DbMessiahService(
    val config: HikariConfig,
    val serializer: DbMessiahSerializer,
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

    fun <T> query(readOnly: Boolean = false, body: (conn: QueryConnection) -> T): T {
        var conn: Connection? = null
        try {
            //Getting connection
            conn = db.connection

            //Will connection be read only
            conn.isReadOnly = readOnly

            //Execute query body and get user result
            val qc = QueryConnection(conn = conn, ser = serializer)
            val returned = body(qc)

            //Close connection
            this.close(conn = conn)

            //Return value
            return returned
        } catch (e: Throwable) {
            this.close(conn = conn)
            throw ServiceException("Unknown execution error", e)
        }
    }

    fun <T> transaction(isolationLevel: IsolationLevel? = null, body: (tr: TransactionConnection) -> T): T {
        var conn: Connection? = null
        try {
            //Getting connection
            conn = db.connection

            //Start transaction
            conn.autoCommit = false

            //Set user defined isolation level
            this.log.info("Transaction type: ${conn.transactionIsolation}")
            if (isolationLevel != null) conn.transactionIsolation = isolationLevel.ordinal

            //Execute transaction body and get user result
            val tr = TransactionConnection(conn = conn, ser = serializer)
            val returned = body(tr)

            //Commit changes and close connection
            conn.commit()
            conn.close()

            //Return value
            return returned
        } catch (e: Throwable) {
            //If any error occurse that is not user handled then rollback, close and raise exception
            this.rollback(conn = conn)
            this.close(conn = conn)
            throw ServiceException("Unknown transaction error", e)
        }
    }
}
