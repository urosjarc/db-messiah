package com.urosjarc.dbmessiah

import java.sql.Connection
import java.sql.Savepoint


class TransConn(private val conn: Connection){
    fun all() = this.conn.rollback()
    fun to(savePoint: Savepoint) = this.conn.rollback(savePoint)
    fun savePoint() = this.conn.setSavepoint()

}
