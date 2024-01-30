package com.urosjarc.dbmessiah.impl

import com.urosjarc.dbmessiah.Serializer
import java.sql.Connection
import java.sql.Savepoint


class DbTransaction(private val conn: Connection, ser: Serializer) : DbConnection(conn = conn, ser = ser) {
    fun rollbackAll() = this.conn.rollback()
    fun rollbackTo(savePoint: Savepoint) = this.conn.rollback(savePoint)
    fun savePoint() = this.conn.setSavepoint()

}
