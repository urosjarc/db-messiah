package com.urosjarc.dbmessiah.impl.db2

import com.urosjarc.dbmessiah.TransactionalConnection
import java.sql.Connection
import java.sql.Savepoint


class Db2TransConn(conn: Connection, ser: OracleSerializer) {

    val tcon = TransactionalConnection(conn = conn, ser = ser)
    fun rollbackAll() = this.tcon.rollbackAll()
    fun rollbackTo(savePoint: Savepoint) = this.tcon.rollbackTo(savePoint = savePoint)
    fun savePoint() = this.tcon.savePoint()
}
