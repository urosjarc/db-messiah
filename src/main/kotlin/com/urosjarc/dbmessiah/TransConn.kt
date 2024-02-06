package com.urosjarc.dbmessiah

import java.sql.Connection
import java.sql.Savepoint


class TransConn(private val conn: Connection){
    fun all() = this.conn.rollback()
    fun to(point: Savepoint) = this.conn.rollback(point)
    fun savePoint() = this.conn.setSavepoint()

}
