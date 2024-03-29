package com.urosjarc.dbmessiah

import com.urosjarc.dbmessiah.domain.Isolation
import com.urosjarc.dbmessiah.exceptions.ConnectionException
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.apache.logging.log4j.kotlin.logger
import java.sql.Connection
import java.util.*

/**
 * This class represents a Service that creates query or transactional connections on which
 * queries can be executed.
 */
public open class ConnectionPool {

    /** @suppress */
    private val log = this.logger()

    /**
     * Configuration for Hikari connection pool.
     *
     * @param props The properties object that contains the specific configuration options.
     */
    private val config: HikariConfig

    /**
     * Represents a pool of database connections from which connections can be fetched.
     *
     * @property source The HikariCP data source instance.
     */
    private val source: HikariDataSource

    /**
     * This constructor creates an instance of the class with the given properties.
     * It initializes the HikariConfig and HikariDataSource objects using the provided properties.
     *
     * @param propsPath The path of the properties file to be used for configuring HikariCP.
     */
    public constructor(props: Properties) {
        this.config = HikariConfig(props)
        this.source = HikariDataSource(this.config)
    }

    /**
     * This constructor creates an instance of the class with the given properties file path.
     * It initializes the HikariConfig and HikariDataSource objects using the provided properties path.
     *
     * @param propsPath The path of the properties file to be used for configuring HikariCP.
     */
    public constructor(propsPath: String) {
        this.config = HikariConfig(propsPath)
        this.source = HikariDataSource(this.config)
    }

    /**
     * Return database connection to connection pool.
     *
     * @param conn The database connection to return to connection pool.
     */
    private fun close(conn: Connection?) {
        try {
            conn?.close()
        } catch (e: Throwable) {
            this.log.error("Closing connection failed", e)
        }
    }

    /**
     * Rollbacks changes on given database connection.
     *
     * @param conn the connection to rollback
     */
    private fun rollback(conn: Connection?) {
        try {
            conn?.rollback()
        } catch (e: Throwable) {
            this.log.error("Rollback connection failed", e)
        }
    }

    /**
     * Fetch available connection from connection pool which will be non-transactional.
     *
     * @param body The query logic to be executed on the connection.
     * @throws ConnectionException if the query was interrupted by an exception.
     */
    public fun autocommit(body: (conn: Connection) -> Unit) {
        var conn: Connection? = null
        try {
            //Getting connection
            conn = source.connection

            //Execute query body and get user result
            body(conn)

            //Close connection
            this.close(conn = conn)

        } catch (e: Throwable) {
            this.close(conn = conn)
            throw ConnectionException("Autocommit was interrupted by unhandled exception", e)
        }
    }

    /**
     * Fetch available connection from connection pool which will be transactional.
     *
     * @param isolation The isolation level for the transaction.
     * @param body The transaction logic to be executed.
     * @throws ConnectionException if an exception occurs during the transaction.
     */
    public fun transaction(isolation: Isolation? = null, body: (conn: Connection) -> Unit) {
        var conn: Connection? = null
        try {
            //Getting connection
            conn = source.connection

            //Start transaction
            conn.autoCommit = false

            //Set user defined isolation level
            this.log.info("Transaction type: ${conn.transactionIsolation}")
            if (isolation != null) conn.transactionIsolation = isolation.ordinal

            //Execute transaction body and get user result
            body(conn)

            //Commit changes and close connection
            conn.commit()
            conn.close()
        } catch (e: Throwable) {
            //If any error occurse that is not user handled then rollback, close and raise exception
            this.rollback(conn = conn)
            this.close(conn = conn)
            throw ConnectionException("Transaction was interrupted by unhandled exception, executing rollback", e)
        }
    }
}
