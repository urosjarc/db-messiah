package com.urosjarc.dbmessiah.impl.postgresql

import com.urosjarc.dbmessiah.Driver
import com.urosjarc.dbmessiah.Service
import com.urosjarc.dbmessiah.domain.Isolation
import com.urosjarc.dbmessiah.domain.Rollback
import com.urosjarc.dbmessiah.queries.*
import java.util.*

public open class PgService : Service<PgSerializer> {
    public constructor(config: Properties, ser: PgSerializer) : super(config = config, ser = ser)
    public constructor(configPath: String, ser: PgSerializer) : super(configPath = configPath, ser = ser)

    public open class Connection(conn: java.sql.Connection, ser: PgSerializer) {
        private val driver = Driver(conn = conn)
        public val schema: SchemaCascadeQueries = SchemaCascadeQueries(ser = ser, driver = driver)
        public val table: TableCascadeQueries = TableCascadeQueries(ser = ser, driver = driver)
        public val row: RowQueries = RowQueries(ser = ser, driver = driver)
        public val batch: BatchQueries = BatchQueries(ser = ser, driver = driver)
        public val query: GetManyQueries = GetManyQueries(ser = ser, driver = driver)
    }

    public fun autocommit(body: (conn: Connection) -> Unit): Unit =
        this.conn.autocommit { body(Connection(conn = it, ser = ser)) }

    public class Transaction(conn: java.sql.Connection, ser: PgSerializer) : Connection(conn = conn, ser = ser) {
        public val roolback: Rollback = Rollback(conn = conn)
    }

    public fun transaction(isolation: Isolation? = null, body: (tr: Transaction) -> Unit): Unit =
        this.conn.transaction(isolation = isolation) { body(Transaction(conn = it, ser = this.ser)) }
}
