package com.urosjarc.dbmessiah.impl.h2

import com.urosjarc.dbmessiah.Driver
import com.urosjarc.dbmessiah.Serializer
import com.urosjarc.dbmessiah.Service
import com.urosjarc.dbmessiah.domain.TransConn
import com.urosjarc.dbmessiah.queries.*
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.util.IsolationLevel
import java.sql.Connection


/**
 * H2Service is a class that provides access to H2 database functionality.
 *
 * @param conf The HikariConfig object containing the database connection configuration.
 * @param ser The Serializer object used for serializing and deserializing data.
 */
public open class H2Service(conf: HikariConfig, private val ser: Serializer) {
    private val service = Service(conf = conf)

    public open class H2QueryConn(conn: Connection, ser: Serializer) {
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
    public fun query(readOnly: Boolean = false, body: (conn: H2QueryConn) -> Unit): Unit =
        this.service.query(readOnly = readOnly) { body(H2QueryConn(conn = it, ser = this.ser)) }

    public class H2TransConn(conn: Connection, ser: Serializer) : H2QueryConn(conn = conn, ser = ser) {
        public val roolback: TransConn = TransConn(conn = conn)
    }

    /**
     * Provides connection on which transactional queries can be executed.
     *
     * @param isolationLevel The isolation level for the transaction.
     * @param body The transaction logic to be executed.
     * @throws ServiceException if an exception occurs during the transaction.
     */
    public fun transaction(isolationLevel: IsolationLevel? = null, body: (tr: H2TransConn) -> Unit): Unit =
        this.service.transaction(isoLevel = isolationLevel) { body(H2TransConn(conn = it, ser = this.ser)) }
}
