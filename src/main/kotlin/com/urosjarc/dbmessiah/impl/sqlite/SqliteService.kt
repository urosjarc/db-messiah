package com.urosjarc.dbmessiah.impl.sqlite

import com.urosjarc.dbmessiah.Driver
import com.urosjarc.dbmessiah.Service
import com.urosjarc.dbmessiah.domain.Isolation
import com.urosjarc.dbmessiah.domain.Rollback
import com.urosjarc.dbmessiah.exceptions.ConnectionException
import com.urosjarc.dbmessiah.queries.BatchQueries
import com.urosjarc.dbmessiah.queries.RowQueries
import com.urosjarc.dbmessiah.queries.GetOneQueries
import com.urosjarc.dbmessiah.queries.TableQueries
import java.util.*


/**
 * This class represents a service for interacting with an SQLite database.
 */
public class SqliteService : Service<SqliteSerializer> {
    public constructor(config: Properties, ser: SqliteSerializer) : super(config = config, ser = ser)
    public constructor(configPath: String, ser: SqliteSerializer) : super(configPath = configPath, ser = ser)

    public open class Connection(conn: java.sql.Connection, ser: SqliteSerializer) {
        private val driver = Driver(conn = conn)
        public val table: TableQueries = TableQueries(ser = ser, driver = driver)
        public val row: RowQueries = RowQueries(ser = ser, driver = driver)
        public val batch: BatchQueries = BatchQueries(ser = ser, driver = driver)
        public val query: GetOneQueries = GetOneQueries(ser = ser, driver = driver)
    }

    /**
     * Provides connection on which non-transactional queries can be executed.
     *
     * @param body The query logic to be executed on the connection.
     * @throws ConnectionException if the query connection was interrupted by an exception.
     */
    public fun autocommit(body: (conn: Connection) -> Unit): Unit =
        this.conn.autocommit { body(Connection(conn = it, ser = ser)) }

    public class Transaction(conn: java.sql.Connection, ser: SqliteSerializer) : Connection(conn = conn, ser = ser) {
        public val roolback: Rollback = Rollback(conn = conn)
    }

    /**
     * Provides connection on which transactional queries can be executed.
     *
     * @param isolation The isolation level for the transaction. Default is null.
     * @param body The transaction logic to be executed on the connection.
     * @throws ConnectionException if an exception occurs during the transaction.
     */
    public fun transaction(isolation: Isolation? = null, body: (tr: Transaction) -> Unit): Unit =
        this.conn.transaction(isolation = isolation) { body(Transaction(conn = it, ser = this.ser)) }
}
