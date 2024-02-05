package com.urosjarc.dbmessiah.impl.oracle

import com.urosjarc.dbmessiah.Driver
import com.urosjarc.dbmessiah.Serializer
import com.urosjarc.dbmessiah.domain.querie.Query
import com.urosjarc.dbmessiah.domain.queries.RowQueries

class OracleRowQueries(ser: Serializer, driver: Driver) : RowQueries(ser = ser, driver = driver) {
    override fun <T : Any> insert(row: T): Boolean {
        val T = this.ser.mapper.getTableInfo(obj = row)

        //If object has pk then reject it since its allready identified
        if (T.primaryKey.getValue(obj = row) != null) return false //Only objects who doesnt have primary key can be inserted!!!

        //Insert it
        val query = this.ser.insertQuery(obj = row, batch = false)
        val query2 = Query(sql = ser.selectLastId(row = row))
        this.driver.execute(query = query) { i, rs -> listOf() }
        val result = this.driver.execute(query = query2) { i, rs -> listOf(rs.getInt(1)) }

        //Set primary key on object
        T.primaryKey.setValue(obj = row, value = result[0][0])

        //Return success
        return true
    }
}
