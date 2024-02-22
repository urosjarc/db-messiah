package com.urosjarc.dbmessiah.impl.db2

import com.urosjarc.dbmessiah.Driver
import com.urosjarc.dbmessiah.Serializer
import com.urosjarc.dbmessiah.Service
import com.urosjarc.dbmessiah.TransConn
import com.urosjarc.dbmessiah.domain.queries.*
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.util.IsolationLevel
import java.sql.Connection

public open class Db2Service(conf: HikariConfig, private val ser: Serializer) {
    private val service = Service(conf = conf)

    public open class Db2QueryConn(conn: Connection, ser: Serializer) {
        private val driver = Driver(conn = conn)
        public val schema: SchemaQueries = SchemaQueries(ser = ser, driver = driver)
        public val table: TableQueries = TableQueries(ser = ser, driver = driver)
        public val row: RowQueries = RowQueries(ser = ser, driver = driver)
        public val batch: BatchQueries = BatchQueries(ser = ser, driver = driver)
        public val call: CallQueries = CallQueries(ser = ser, driver = driver)
        public val run: RunOneQueries = RunOneQueries(ser = ser, driver = driver)
    }

    public fun query(readOnly: Boolean = false, body: (conn: Db2QueryConn) -> Unit): Unit =
        this.service.query(readOnly = readOnly) { body(Db2QueryConn(conn = it, ser = this.ser)) }

    public class Db2TransConn(conn: Connection, ser: Serializer) : Db2QueryConn(conn = conn, ser = ser) {
        public val roolback: TransConn = TransConn(conn = conn)
    }

    public fun transaction(isolationLevel: IsolationLevel? = null, body: (tr: Db2TransConn) -> Unit): Unit =
        this.service.transaction(isoLevel = isolationLevel) { body(Db2TransConn(conn = it, ser = this.ser)) }
}
