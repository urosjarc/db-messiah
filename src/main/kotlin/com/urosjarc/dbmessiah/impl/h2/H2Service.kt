package com.urosjarc.dbmessiah.impl.h2

import com.urosjarc.dbmessiah.Driver
import com.urosjarc.dbmessiah.Service
import com.urosjarc.dbmessiah.domain.Isolation
import com.urosjarc.dbmessiah.domain.Rollback
import com.urosjarc.dbmessiah.queries.*
import java.util.*


/**
 * H2Service is a class that provides access to H2 database functionality.
 */
public open class H2Service : Service<H2Serializer> {
    public constructor(config: Properties, ser: H2Serializer) : super(config = config, ser = ser)
    public constructor(configPath: String, ser: H2Serializer) : super(configPath = configPath, ser = ser)

    public open class Connection(conn: java.sql.Connection, ser: H2Serializer) {
        private val driver = Driver(conn = conn)
        public val schema: SchemaCascadeQueries = SchemaCascadeQueries(ser = ser, driver = driver)
        public val table: TableCascadeQueries = TableCascadeQueries(ser = ser, driver = driver)
        public val row: RowQueries = RowQueries(ser = ser, driver = driver)
        public val batch: BatchQueries = BatchQueries(ser = ser, driver = driver)
        public val query: GetOneQueries = GetOneQueries(ser = ser, driver = driver)
        public val procedure: ProcedureQueries = ProcedureQueries(ser = ser, driver = driver)
    }

    /**
     * Provides connection on which non-transactional queries can be executed.
     *
     * @param readOnly Specifies whether the connection should be read-only.
     * @param body The query logic to be executed on the connection.
     * @throws ServiceException if the query was interrupted by an exception.
     */
    public fun autocommit(body: (conn: Connection) -> Unit): Unit =
        this.conn.autocommit { body(Connection(conn = it, ser = ser)) }

    public class Transaction(conn: java.sql.Connection, ser: H2Serializer) : Connection(conn = conn, ser = ser) {
        public val roolback: Rollback = Rollback(conn = conn)
    }

    /**
     * Provides connection on which transactional queries can be executed.
     *
     * @param isolation The isolation level for the transaction.
     * @param body The transaction logic to be executed.
     * @throws ServiceException if an exception occurs during the transaction.
     */
    public fun transaction(isolation: Isolation? = null, body: (tr: Transaction) -> Unit): Unit =
        this.conn.transaction(isolation = isolation) { body(Transaction(conn = it, ser = this.ser)) }
}
