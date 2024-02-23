package com.urosjarc.dbmessiah.impl.db2

import com.urosjarc.dbmessiah.Driver
import com.urosjarc.dbmessiah.Serializer
import com.urosjarc.dbmessiah.Service
import com.urosjarc.dbmessiah.domain.Isolation
import com.urosjarc.dbmessiah.domain.TransConn
import com.urosjarc.dbmessiah.queries.*
import java.sql.Connection
import java.util.*

/**
 * Db2Service class provides a high-level interface for interacting with a Db2 database.
 */
public open class Db2Service : Service {
    public constructor(config: Properties, ser: Serializer) : super(config = config, ser = ser)
    public constructor(configPath: String, ser: Serializer) : super(configPath = configPath, ser = ser)

    public open class Db2QueryConn(conn: Connection, ser: Serializer) {
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
     */
    public fun query(readOnly: Boolean = false, body: (conn: Db2QueryConn) -> Unit): Unit =
        this.conn.query(readOnly = readOnly) { body(Db2QueryConn(conn = it, ser = this.ser)) }

    public class Db2TransConn(conn: Connection, ser: Serializer) : Db2QueryConn(conn = conn, ser = ser) {
        public val roolback: TransConn = TransConn(conn = conn)
    }

    /**
     * Provides connection on which transactional queries can be executed.
     *
     * @param isolation The isolation level for the transaction. Default is null.
     * @param body The transaction logic to be executed.
     */
    public fun transaction(isolation: Isolation? = null, body: (tr: Db2TransConn) -> Unit): Unit =
        this.conn.transaction(isolation = isolation) { body(Db2TransConn(conn = it, ser = this.ser)) }
}
