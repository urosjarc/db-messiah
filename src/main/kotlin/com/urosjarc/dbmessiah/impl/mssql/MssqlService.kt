package com.urosjarc.dbmessiah.impl.mssql

import com.urosjarc.dbmessiah.Driver
import com.urosjarc.dbmessiah.Serializer
import com.urosjarc.dbmessiah.Service
import com.urosjarc.dbmessiah.domain.TransConn
import com.urosjarc.dbmessiah.queries.*
import com.zaxxer.hikari.util.IsolationLevel
import java.sql.Connection
import java.util.*

/**
 * Represents a service for executing queries on a Microsoft SQL Server database using HikariCP connection pooling.
 */
public open class MssqlService : Service {
    public constructor(config: Properties, ser: Serializer) : super(config = config, ser = ser)
    public constructor(configPath: String, ser: Serializer) : super(configPath = configPath, ser = ser)

    public open class MssqlQueryConn(conn: Connection, ser: Serializer) {
        private val driver = Driver(conn = conn)
        public val schema: SchemaQueries = SchemaQueries(ser = ser, driver = driver)
        public val table: TableQueries = TableQueries(ser = ser, driver = driver)
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
    public fun query(readOnly: Boolean = false, body: (conn: MssqlQueryConn) -> Unit): Unit =
        this.conn.query(readOnly = readOnly) { body(MssqlQueryConn(conn = it, ser = this.ser)) }

    public class MssqlTransConn(conn: Connection, ser: Serializer) : MssqlQueryConn(conn = conn, ser = ser) {
        public val roolback: TransConn = TransConn(conn = conn)
    }

    /**
     * Provides connection on which transactional queries can be executed.
     *
     * @param isolationLevel The isolation level for the transaction. Default is null.
     * @param body The transaction logic to be executed on the connection.
     * @throws ServiceException if an exception occurs during the transaction.
     */
    public fun transaction(isolationLevel: IsolationLevel? = null, body: (tr: MssqlTransConn) -> Unit): Unit =
        this.conn.transaction(isoLevel = isolationLevel) { body(MssqlTransConn(conn = it, ser = this.ser)) }
}
