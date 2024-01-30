package com.urosjarc.dbmessiah

import java.sql.Connection
import java.sql.Savepoint


class TransactionConnection(private val conn: Connection, ser: DbMessiahSerializer) : QueryConnection(conn = conn, ser = ser) {
    fun rollbackAll() = this.conn.rollback()
    fun rollbackTo(savePoint: Savepoint) = this.conn.rollback(savePoint)
    fun savePoint() = this.conn.setSavepoint()

}
