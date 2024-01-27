package com.urosjarc.dbmessiah.sqlite

import com.urosjarc.dbmessiah.Engine
import com.urosjarc.dbmessiah.Serializer
import com.urosjarc.dbmessiah.domain.queries.PreparedQuery
import com.urosjarc.dbmessiah.impl.DbMessiahService

class SqliteService(
    eng: Engine,
    ser: Serializer,
) : DbMessiahService(eng = eng, ser = ser) {
    override fun <T : Any> insertTable(obj: T): Int? {
        val query = this.ser.insertQuery(obj = obj)
        val pQuery = this.eng.prepareQuery(query = query)
        this.eng.executeInsert(pQuery = pQuery) { rs, i ->

            rs.getInt(i)

        }
    }
}
