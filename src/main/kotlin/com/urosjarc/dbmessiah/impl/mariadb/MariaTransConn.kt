package com.urosjarc.dbmessiah.impl.mariadb

import com.urosjarc.dbmessiah.TransactionalConnection
import java.sql.Connection
import java.sql.Savepoint


class MariaTransConn(conn: Connection, ser: MariaSerializer) {

    val tcon = TransactionalConnection(conn = conn, ser = ser)
    fun rollbackAll() = this.tcon.rollbackAll()
    fun rollbackTo(savePoint: Savepoint) = this.tcon.rollbackTo(savePoint = savePoint)
    fun savePoint() = this.tcon.savePoint()
}
