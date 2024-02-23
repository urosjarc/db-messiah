package com.urosjarc.dbmessiah.impl.h2

import com.urosjarc.dbmessiah.Driver
import com.urosjarc.dbmessiah.Serializer
import com.urosjarc.dbmessiah.Service
import com.urosjarc.dbmessiah.domain.TransConn
import com.urosjarc.dbmessiah.queries.*
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.util.IsolationLevel
import java.sql.Connection


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

    public fun query(readOnly: Boolean = false, body: (conn: H2QueryConn) -> Unit): Unit =
        this.service.query(readOnly = readOnly) { body(H2QueryConn(conn = it, ser = this.ser)) }

    public class H2TransConn(conn: Connection, ser: Serializer) : H2QueryConn(conn = conn, ser = ser) {
        public val roolback: TransConn = TransConn(conn = conn)
    }

    public fun transaction(isolationLevel: IsolationLevel? = null, body: (tr: H2TransConn) -> Unit): Unit =
        this.service.transaction(isoLevel = isolationLevel) { body(H2TransConn(conn = it, ser = this.ser)) }
}
