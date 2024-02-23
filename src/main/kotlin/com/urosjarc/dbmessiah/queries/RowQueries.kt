package com.urosjarc.dbmessiah.queries

import com.urosjarc.dbmessiah.Driver
import com.urosjarc.dbmessiah.Serializer
import com.urosjarc.dbmessiah.exceptions.DriverException
import kotlin.reflect.KClass

public open class RowQueries(
    private val ser: Serializer,
    private val driver: Driver
) {
    public fun <T : Any, K : Any> select(table: KClass<T>, pk: K): T? {
        val query = this.ser.query(kclass = table, pk = pk)
        return this.driver.query(query = query) {
            this.ser.mapper.decode(resultSet = it, kclass = table)
        }.firstOrNull()
    }

    public open fun <T : Any> insert(row: T): Boolean {
        val T = this.ser.mapper.getTableInfo(obj = row)

        //If object has pk then reject it since its allready identified
        if (T.primaryKey.getValue(obj = row) != null) return false //Only objects who doesnt have primary key can be inserted!!!

        //Insert it
        val query = this.ser.insertQuery(obj = row, batch = false)
        val selectLastId = this.ser.selectLastId
        val pk = this.driver.insert(query = query, onGeneratedKeysFail = selectLastId) { rs, i -> rs.getInt(i) }

        //If pk didn't retrieved insert didn't happend
        if (pk == null) return false

        //Set primary key on object
        T.primaryKey.setValue(obj = row, value = pk)

        //Return success
        return true
    }

    public fun <T : Any> update(row: T): Boolean {
        val T = this.ser.mapper.getTableInfo(obj = row)

        //If object has not pk then reject since it must be first created
        if (T.primaryKey.getValue(obj = row) == null) return false //Only objects who doesnt have primary key can be inserted!!!

        //Update object
        val query = this.ser.updateQuery(obj = row)

        //Return number of updates
        val count = this.driver.update(query = query)

        //Success if only 1
        if (count == 1) return true
        else if (count == 0) return false
        else throw DriverException("Number of updated rows must be 1 or 0 but number of updated rows was: $count")
    }

    public fun <T : Any> delete(row: T): Boolean {
        val T = this.ser.mapper.getTableInfo(obj = row)

        //If object has not pk then reject since it must be first created
        if (T.primaryKey.getValue(obj = row) == null) return false //Only objects who doesnt have primary key can be inserted!!!

        //Delete object if primary key exists
        val query = this.ser.deleteQuery(obj = row)

        //Update rows and get change count
        val count = this.driver.update(query = query)

        //Success if only 1
        if (count == 0) return false
        else if (count == 1) {
            T.primaryKey.setValue(obj = row, value = null)
            return true
        } else throw DriverException("Number of deleted rows must be 1 or 0 but number of updated rows was: $count")
    }

    public fun <T : Any> insert(rows: Iterable<T>): List<Boolean> = rows.map { this.insert(row = it) }

    public fun <T : Any> update(rows: Iterable<T>): List<Boolean> = rows.map { this.update(row = it) }

    public fun <T : Any> delete(rows: Iterable<T>): List<Boolean> = rows.map { this.delete(row = it) }
}
