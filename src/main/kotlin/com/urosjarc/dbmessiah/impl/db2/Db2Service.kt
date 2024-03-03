package com.urosjarc.dbmessiah.impl.db2

import com.urosjarc.dbmessiah.Driver
import com.urosjarc.dbmessiah.Service
import com.urosjarc.dbmessiah.domain.Isolation
import com.urosjarc.dbmessiah.domain.Rollback
import com.urosjarc.dbmessiah.queries.*
import java.util.*

public open class Db2Service : Service<Db2Serializer> {
    public constructor(config: Properties, ser: Db2Serializer) : super(config = config, ser = ser)
    public constructor(configPath: String, ser: Db2Serializer) : super(configPath = configPath, ser = ser)

    public open class Connection(conn: java.sql.Connection, ser: Db2Serializer) {
        private val driver = Driver(conn = conn)
        public val schema: SchemaQueries = SchemaQueries(ser = ser, driver = driver)
        public val table: TableQueries = TableQueries(ser = ser, driver = driver)
        public val row: RowQueries = RowQueries(ser = ser, driver = driver)
        public val batch: BatchQueries = BatchQueries(ser = ser, driver = driver)
        public val query: GetOneQueries = GetOneQueries(ser = ser, driver = driver)
        public val procedure: NoReturnProcedureQueries = NoReturnProcedureQueries(ser = ser, driver = driver)
    }

    public fun autocommit(body: (conn: Connection) -> Unit): Unit =
        this.conn.autocommit { body(Connection(conn = it, ser = ser)) }

    public class Transaction(conn: java.sql.Connection, ser: Db2Serializer) : Connection(conn = conn, ser = ser) {
        public val roolback: Rollback = Rollback(conn = conn)
    }

    /**
     * Provides connection on which transactional queries can be executed.
     *
     * @param isolation The isolation level for the transaction. Default is null.
     * @param body The transaction logic to be executed.
     */
    public fun transaction(isolation: Isolation? = null, body: (tr: Transaction) -> Unit): Unit =
        this.conn.transaction(isolation = isolation) { body(Transaction(conn = it, ser = this.ser)) }
}
