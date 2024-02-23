package com.urosjarc.dbmessiah.impl.postgresql

import com.urosjarc.dbmessiah.Driver
import com.urosjarc.dbmessiah.Serializer
import com.urosjarc.dbmessiah.Service
import com.urosjarc.dbmessiah.domain.Isolation
import com.urosjarc.dbmessiah.domain.TransConn
import com.urosjarc.dbmessiah.queries.*
import java.sql.Connection
import java.util.*

/**
 * Represents a PostgreSQL service that provides methods for interacting with a PostgreSQL database.
 */
public open class PgService : Service {
    public constructor(config: Properties, ser: Serializer) : super(config = config, ser = ser)
    public constructor(configPath: String, ser: Serializer) : super(configPath = configPath, ser = ser)

    public open class PgQueryConn(conn: Connection, ser: Serializer) {
        private val driver = Driver(conn = conn)
        public val schema: SchemaQueries = SchemaQueries(ser = ser, driver = driver)
        public val table: TableCascadeQueries = TableCascadeQueries(ser = ser, driver = driver)
        public val row: RowQueries = RowQueries(ser = ser, driver = driver)
        public val batch: BatchQueries = BatchQueries(ser = ser, driver = driver)
        public val run: RunManyQueries = RunManyQueries(ser = ser, driver = driver)
    }

    /**
     * Provides connection on which non-transactional queries can be executed.
     *
     * @param readOnly Specifies whether the connection should be read-only.
     * @param body The query logic to be executed on the connection.
     * @throws ServiceException if the query was interrupted by an exception.
     */
    public fun query(readOnly: Boolean = false, body: (conn: PgQueryConn) -> Unit): Unit =
        this.conn.query(readOnly = readOnly) { body(PgQueryConn(conn = it, ser = this.ser)) }

    public class PgTransConn(conn: Connection, ser: Serializer) : PgQueryConn(conn = conn, ser = ser) {
        public val roolback: TransConn = TransConn(conn = conn)
    }

    /**
     * Provides connection on which transactional queries can be executed.
     *
     * @param isolation The isolation level for the transaction. Default is null.
     * @param body The transaction logic to be executed on the connection.
     * @throws ServiceException if an exception occurs during the transaction.
     */
    public fun transaction(isolation: Isolation? = null, body: (tr: PgTransConn) -> Unit): Unit =
        this.conn.transaction(isolation = isolation) { body(PgTransConn(conn = it, ser = this.ser)) }
}
