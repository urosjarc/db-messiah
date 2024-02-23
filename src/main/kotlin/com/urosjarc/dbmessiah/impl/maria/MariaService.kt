package com.urosjarc.dbmessiah.impl.maria

import com.urosjarc.dbmessiah.Driver
import com.urosjarc.dbmessiah.Serializer
import com.urosjarc.dbmessiah.Service
import com.urosjarc.dbmessiah.domain.TransConn
import com.urosjarc.dbmessiah.queries.*
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.util.IsolationLevel
import java.sql.Connection

/**
 * The `MariaService` class provides functionality to interact with a MariaDB database.
 *
 * @param conf The HikariConfig object used for configuring the database connection.
 * @param ser The Serializer object used for serializing and deserializing data.
 */
public open class MariaService(conf: HikariConfig, private val ser: Serializer) {
    private val service = Service(conf = conf)

    public open class MariaQueryConn(conn: Connection, ser: Serializer) {
        private val driver = Driver(conn = conn)
        public val schema: SchemaQueries = SchemaQueries(ser = ser, driver = driver)
        public val table: TableQueries = TableQueries(ser = ser, driver = driver)
        public val row: RowQueries = RowQueries(ser = ser, driver = driver)
        public val batch: BatchQueries = BatchQueries(ser = ser, driver = driver)
        public val call: CallQueries = CallQueries(ser = ser, driver = driver)
        public val run: RunOneQueries = RunOneQueries(ser = ser, driver = driver)
    }

    /**
     * Provides connection on which non-transactional queries can be executed.
     *
     * @param readOnly Specifies whether the connection should be read-only.
     * @param body The query logic to be executed on the connection.
     * @throws ServiceException if the query was interrupted by an exception.
     */
    public fun query(readOnly: Boolean = false, body: (conn: MariaQueryConn) -> Unit): Unit =
        this.service.query(readOnly = readOnly) { body(MariaQueryConn(conn = it, ser = this.ser)) }

    public class MariaTransConn(conn: Connection, ser: Serializer) : MariaQueryConn(conn = conn, ser = ser) {
        public val roolback: TransConn = TransConn(conn = conn)
    }

    /**
     * Provides connection on which transactional queries can be executed.
     *
     * @param isolationLevel The isolation level for the transaction.
     * @param body The logic to be executed within the transaction.
     * @throws ServiceException if an exception occurs during the transaction.
     */
    public fun transaction(isolationLevel: IsolationLevel? = null, body: (tr: MariaTransConn) -> Unit): Unit =
        this.service.transaction(isoLevel = isolationLevel) { body(MariaTransConn(conn = it, ser = this.ser)) }
}
