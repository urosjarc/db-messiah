package com.urosjarc.dbmessiah.impl.derby

import com.urosjarc.dbmessiah.Driver
import com.urosjarc.dbmessiah.Serializer
import com.urosjarc.dbmessiah.Service
import com.urosjarc.dbmessiah.TransConn
import com.urosjarc.dbmessiah.domain.queries.*
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.util.IsolationLevel
import java.sql.Connection


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

    public fun query(readOnly: Boolean = false, body: (conn: DerbyQueryConn) -> Unit): Unit =
        this.service.query(readOnly = readOnly) { body(DerbyQueryConn(conn = it, ser = this.ser)) }

    public class DerbyTransConn(conn: Connection, ser: Serializer) : DerbyQueryConn(conn = conn, ser = ser) {
        public val roolback: TransConn = TransConn(conn = conn)
    }

    public fun transaction(isolationLevel: IsolationLevel? = null, body: (tr: DerbyTransConn) -> Unit): Unit =
        this.service.transaction(isoLevel = isolationLevel) { body(DerbyTransConn(conn = it, ser = this.ser)) }
}
