package com.urosjarc.dbmessiah.impl.derby

import com.urosjarc.dbmessiah.Driver
import com.urosjarc.dbmessiah.Serializer
import com.urosjarc.dbmessiah.Service
import com.urosjarc.dbmessiah.domain.TransConn
import com.urosjarc.dbmessiah.queries.*
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.util.IsolationLevel
import java.sql.Connection


/**
 * The `DerbyService` class is responsible for interacting with a Derby database.
 * It provides methods for executing queries and transactions.
 *
 * @property conf The configuration for the underlying `Service`.
 * @property ser The serializer used for serializing and deserializing objects.
 */
public open class DerbyService(conf: HikariConfig, private val ser: Serializer) {
    private val service = Service(conf = conf)

    public open class DerbyQueryConn(conn: Connection, ser: Serializer) {
        private val driver = Driver(conn = conn)
        public val schema: SchemaQueries = SchemaQueries(ser = ser, driver = driver)
        public val table: TableCascadeQueries = TableCascadeQueries(ser = ser, driver = driver)
        public val row: RowQueries = RowQueries(ser = ser, driver = driver)
        public val batch: BatchQueries = BatchQueries(ser = ser, driver = driver)
        public val run: RunOneQueries = RunOneQueries(ser = ser, driver = driver)
    }

    /**
     * Provides connection on which non-transactional queries can be executed.
     *
     * @param readOnly Specifies whether the connection should be read-only.
     * @param body The query logic to be executed on the connection.
     * @throws ServiceException if the query was interrupted by an exception.
     */
    public fun query(readOnly: Boolean = false, body: (conn: DerbyQueryConn) -> Unit): Unit =
        this.service.query(readOnly = readOnly) { body(DerbyQueryConn(conn = it, ser = this.ser)) }

    public class DerbyTransConn(conn: Connection, ser: Serializer) : DerbyQueryConn(conn = conn, ser = ser) {
        public val roolback: TransConn = TransConn(conn = conn)
    }

    /**
     * Provides connection on which transactional queries can be executed.
     *
     * @param isolationLevel The isolation level for the transaction. Default is null.
     * @param body The transaction logic to be executed on the connection.
     * @throws ServiceException if an exception occurs during the transaction.
     */
    public fun transaction(isolationLevel: IsolationLevel? = null, body: (tr: DerbyTransConn) -> Unit): Unit =
        this.service.transaction(isoLevel = isolationLevel) { body(DerbyTransConn(conn = it, ser = this.ser)) }
}
