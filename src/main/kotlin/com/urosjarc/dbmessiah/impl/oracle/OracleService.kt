package com.urosjarc.dbmessiah.impl.oracle

import com.urosjarc.dbmessiah.Driver
import com.urosjarc.dbmessiah.Service
import com.urosjarc.dbmessiah.domain.Isolation
import com.urosjarc.dbmessiah.domain.Rollback
import com.urosjarc.dbmessiah.queries.*
import java.util.*

public open class OracleService : Service<OracleSerializer> {
    public constructor(config: Properties, ser: OracleSerializer) : super(config = config, ser = ser)
    public constructor(configPath: String, ser: OracleSerializer) : super(configPath = configPath, ser = ser)

    public open class Connection(conn: java.sql.Connection, ser: OracleSerializer) {
        private val driver = Driver(conn = conn)
        public val table: TableCascadeQueries = TableCascadeQueries(ser = ser, driver = driver)
        public val row: RowQueries = RowQueries(ser = ser, driver = driver)
        public val batch: BatchQueries = BatchQueries(ser = ser, driver = driver)
        public val query: GetOneQueries = GetOneQueries(ser = ser, driver = driver)
        public val procedure: NoReturnProcedureQueries = NoReturnProcedureQueries(ser = ser, driver = driver)
    }

    public fun autocommit(body: (conn: Connection) -> Unit): Unit =
        this.conn.autocommit { body(Connection(conn = it, ser = ser)) }

    public class Transaction(conn: java.sql.Connection, ser: OracleSerializer) : Connection(conn = conn, ser = ser) {
        public val roolback: Rollback = Rollback(conn = conn)
    }

    public fun transaction(isolation: Isolation? = null, body: (tr: Transaction) -> Unit): Unit =
        this.conn.transaction(isolation = isolation) { body(Transaction(conn = it, ser = this.ser)) }
}
