package com.urosjarc.dbmessiah

import com.urosjarc.dbmessiah.domain.queries.*
import com.urosjarc.dbmessiah.exceptions.EngineException
import java.sql.Connection
import kotlin.reflect.KClass


open class QueryConnection(conn: Connection, private val ser: DbMessiahSerializer) {

    private val engine = DbMessiahEngine(conn = conn)

    fun <T : Any> drop(kclass: KClass<T>): Int {
        val query = this.ser.dropQuery(kclass = kclass)
        return this.engine.update(query = query)
    }

    fun <T : Any> create(kclass: KClass<T>): Int {
        val query = this.ser.createQuery(kclass = kclass)
        return this.engine.update(query = query)
    }

    fun <T : Any> select(kclass: KClass<T>): List<T> {
        val query = this.ser.selectQuery(kclass = kclass)
        return this.engine.query(query = query) {
            this.ser.mapper.decode(resultSet = it, kclass = kclass)
        }
    }

    fun <T : Any, K : Any> select(kclass: KClass<T>, pk: K): T? {
        val query = this.ser.selectQuery(kclass = kclass, pk = pk)
        return this.engine.query(query = query) {
            this.ser.mapper.decode(resultSet = it, kclass = kclass)
        }.firstOrNull()
    }

    fun <T : Any> select(kclass: KClass<T>, page: Page<T>): List<T> {
        val query = this.ser.selectQuery(kclass = kclass, page = page)
        return this.engine.query(query = query) {
            this.ser.mapper.decode(resultSet = it, kclass = kclass)
        }
    }

    fun <T : Any> insert(obj: T): Boolean {
        val T = this.ser.mapper.getTableInfo(obj = obj)

        //If object has pk then reject it since its allready identified
        if (T.primaryKey.getValue(obj = obj) != null) return false //Only objects who doesnt have primary key can be inserted!!!

        //Insert it
        val query = this.ser.insertQuery(obj = obj)
        val pk = this.engine.insert(query = query, onGeneratedKeysFail = this.ser.onGeneratedKeysFail) { rs, i -> rs.getInt(i) }

        //If pk didn't retrieved insert didn't happend
        if (pk == null) return false

        //Set primary key on object
        T.primaryKey.setValue(obj = obj, value = pk)

        //Return success
        return true
    }

    fun <T : Any> insertBatch(vararg objs: T): Int {
        val T = this.ser.mapper.getTableInfo(obj = objs[0])

        //Filter only those whos primary key is null
        val fobjs = objs.filter { T.primaryKey.getValue(it) == null }

        //If no object has free primary key then finish
        if (fobjs.isEmpty()) return 0

        //Insert it to db
        val query = this.ser.insertQuery(obj = fobjs[0])

        //Execute query
        val batchQuery = BatchQuery(sql = query.sql, valueMatrix = fobjs.map { T.queryValues(obj = it).toList() })

        //Return count of updated elements
        return this.engine.batch(batchQuery = batchQuery)
    }

    fun <T : Any> update(obj: T): Boolean {
        val T = this.ser.mapper.getTableInfo(obj = obj)

        //If object has not pk then reject since it must be first created
        if (T.primaryKey.getValue(obj = obj) == null) return false //Only objects who doesnt have primary key can be inserted!!!

        //Update object
        val query = this.ser.updateQuery(obj = obj)

        //Return number of updates
        val count = this.engine.update(query = query)

        //Success if only 1
        if (count == 1) return true
        else if (count == 0) return false
        else throw EngineException("Number of updated rows must be 1 or 0 but number of updated rows was: $count")
    }

    fun <T : Any> updateBatch(vararg objs: T): Int {
        val T = this.ser.mapper.getTableInfo(obj = objs[0])

        //Filter only those whos primary key is null
        val fobjs = objs.filter { T.primaryKey.getValue(it) != null }

        //If no object has free primary key then finish
        if (fobjs.isEmpty()) return 0

        //Update objects
        val query = this.ser.updateQuery(obj = fobjs[0])
        val valueMatrix = fobjs.map { listOf(*T.queryValues(obj = it), T.primaryKey.queryValue(obj = it)) }
        val batchQuery = BatchQuery(sql = query.sql, valueMatrix = valueMatrix)

        //Return result
        return this.engine.batch(batchQuery = batchQuery)
    }

    fun <T : Any> delete(obj: T): Boolean {
        val T = this.ser.mapper.getTableInfo(obj = obj)

        //If object has not pk then reject since it must be first created
        if (T.primaryKey.getValue(obj = obj) == null) return false //Only objects who doesnt have primary key can be inserted!!!

        //Delete object if primary key exists
        val query = this.ser.deleteQuery(obj = obj)

        //Update rows and get change count
        val count = this.engine.update(query = query)

        //Success if only 1
        if (count == 0) return false
        else if (count == 1) {
            T.primaryKey.setValue(obj = obj, value = null)
            return true
        } else throw EngineException("Number of deleted rows must be 1 or 0 but number of updated rows was: $count")
    }

    fun <T : Any> delete(kclass: KClass<T>): Int {
        val query = this.ser.deleteQuery(kclass = kclass)
        return this.engine.update(query = query)
    }

    fun <T : Any> deleteBatch(vararg objs: T): Int {
        val T = this.ser.mapper.getTableInfo(obj = objs[0])

        //Filter only those whos primary key is not null
        val fobjs = objs.filter { T.primaryKey.getValue(it) != null }

        //If no object has free primary key then finish
        if (fobjs.isEmpty()) return 0

        //Delete objects
        val query = this.ser.deleteQuery(obj = fobjs[0])
        val valueMatrix = fobjs.map { listOf(T.primaryKey.queryValue(obj = it)) }
        val batchQuery = BatchQuery(sql = query.sql, valueMatrix = valueMatrix)

        //Return result
        return this.engine.batch(batchQuery = batchQuery)
    }

    fun execute(getSql: (queryBuilder: QueryBuilder) -> String) {
        val query = this.ser.selectQuery(getSql = getSql)
        this.engine.execute(query = query) { i, rs -> }
    }

    fun query(getSql: (queryBuilder: QueryBuilder) -> String): Int {
        val query = this.ser.selectQuery(getSql = getSql)
        return this.engine.update(query = query)
    }

    fun <OUT : Any> query(output: KClass<OUT>, getSql: (queryBuilder: QueryBuilderOut<OUT>) -> String): List<OUT> {
        val query = this.ser.selectQuery(output = output, getSql = getSql)
        return this.engine.query(query = query) {
            this.ser.mapper.decode(resultSet = it, kclass = output)
        }
    }

    fun <IN : Any, OUT : Any> query(input: IN, output: KClass<OUT>, getSql: (queryBuilder: QueryBuilderInOut<IN, OUT>) -> String): List<OUT> {
        val query = this.ser.selectQuery(input = input, output = output, getSql = getSql)
        return this.engine.query(query = query) {
            this.ser.mapper.decode(resultSet = it, kclass = output)
        }
    }

    fun <IN : Any, OUT : Any> call(input: IN, output: KClass<OUT>): List<OUT> {
        val query = this.ser.callQuery(input = input)
        return this.engine.query(query = query) {
            this.ser.mapper.decode(resultSet = it, kclass = output)
        }
    }
}
