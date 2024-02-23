package com.urosjarc.dbmessiah.impl.maria

import com.urosjarc.dbmessiah.Driver
import com.urosjarc.dbmessiah.Serializer
import com.urosjarc.dbmessiah.Service
import com.urosjarc.dbmessiah.domain.TransConn
import com.urosjarc.dbmessiah.queries.*
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.util.IsolationLevel
import java.sql.Connection

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

    public fun query(readOnly: Boolean = false, body: (conn: MariaQueryConn) -> Unit): Unit =
        this.service.query(readOnly = readOnly) { body(MariaQueryConn(conn = it, ser = this.ser)) }

    public class MariaTransConn(conn: Connection, ser: Serializer) : MariaQueryConn(conn = conn, ser = ser) {
        public val roolback: TransConn = TransConn(conn = conn)
    }

    public fun transaction(isolationLevel: IsolationLevel? = null, body: (tr: MariaTransConn) -> Unit): Unit =
        this.service.transaction(isoLevel = isolationLevel) { body(MariaTransConn(conn = it, ser = this.ser)) }
}
