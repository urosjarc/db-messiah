package com.urosjarc.dbmessiah.domain

import java.sql.Connection
import java.sql.Savepoint


public class TransConn(private val conn: Connection){
    public fun all(): Unit = this.conn.rollback()
    public fun to(point: Savepoint): Unit = this.conn.rollback(point)
    public fun savePoint(): Savepoint = this.conn.setSavepoint()

}
