package com.urosjarc.dbmessiah

import com.urosjarc.dbmessiah.domain.queries.*
import java.sql.Connection
import kotlin.reflect.KClass


open class QueryConnection(conn: Connection, private val ser: DbMessiahSerializer) {

    private val exe = DbMessiahExecutor(conn = conn)

    fun <T : Any> drop(kclass: KClass<T>): Int {
        val query = this.ser.dropQuery(kclass = kclass)
        return this.exe.update(query = query)
    }

    fun <T : Any> create(kclass: KClass<T>): Int {
        val query = this.ser.createQuery(kclass = kclass)
        return this.exe.update(query = query)
    }

    fun <T : Any> select(kclass: KClass<T>): List<T> {
        val query = this.ser.selectQuery(kclass = kclass)
        return this.exe.query(query = query) {
            this.ser.repo.decode(resultSet = it, kclass = kclass)
        }
    }

    fun <T : Any, K : Any> select(kclass: KClass<T>, pk: K): T? {
        val query = this.ser.selectQuery(kclass = kclass, pk = pk)
        return this.exe.query(query = query) {
            this.ser.repo.decode(resultSet = it, kclass = kclass)
        }.firstOrNull()
    }

    fun <T : Any> select(kclass: KClass<T>, page: Page<T>): List<T> {
        val query = this.ser.selectQuery(kclass = kclass, page = page)
        return this.exe.query(query = query) {
            this.ser.repo.decode(resultSet = it, kclass = kclass)
        }
    }

    fun <T : Any> insert(obj: T): Boolean {
        val T = this.ser.repo.getTableInfo(obj = obj)
        val query = this.ser.insertQuery(obj = obj)
        val id = this.exe.insert(query = query, onGeneratedKeysFail = this.ser.onGeneratedKeysFail) { rs, i -> rs.getInt(i) }
        T.primaryKey.setValue(obj = obj, value = id)
        return true
    }

    fun <T : Any> insertBatch(vararg objs: T): Int {
        val T = this.ser.repo.getTableInfo(obj = objs[0])
        val query = this.ser.insertQuery(obj = objs[0])
        val batchQuery = BatchQuery(sql = query.sql, valueMatrix = objs.map { T.queryValues(obj = it).toList() })
        return this.exe.batch(batchQuery = batchQuery)
    }

    fun <T : Any> updateBatch(vararg objs: T): Int {
        val T = this.ser.repo.getTableInfo(obj = objs[0])
        val query = this.ser.updateQuery(obj = objs[0])
        val valueMatrix = objs.map { listOf(*T.queryValues(obj = it), T.primaryKey.queryValue(obj = it)) }
        val batchQuery = BatchQuery(sql = query.sql, valueMatrix = valueMatrix)
        return this.exe.batch(batchQuery = batchQuery)
    }

    fun <T : Any> update(obj: T): Boolean {
        val query = this.ser.updateQuery(obj = obj)
        return this.exe.update(query = query) == 0
    }

    fun <T : Any> delete(obj: T): Boolean {
        val T = this.ser.repo.getTableInfo(obj = obj)
        val query = this.ser.deleteQuery(obj = obj)
        val deleted = this.exe.update(query = query) == 1
        if (deleted) T.primaryKey.setValue(obj = obj, value = null)
        return deleted
    }

    fun <T : Any> delete(kclass: KClass<T>): Int {
        val query = this.ser.deleteQuery(kclass = kclass)
        return this.exe.update(query = query)
    }

    fun <T : Any> deleteBatch(vararg objs: T): Int {
        val T = this.ser.repo.getTableInfo(obj = objs[0])
        val query = this.ser.deleteQuery(obj = objs[0])
        val valueMatrix = objs.map { listOf(T.primaryKey.queryValue(obj = it)) }
        val batchQuery = BatchQuery(sql = query.sql, valueMatrix = valueMatrix)
        return this.exe.batch(batchQuery = batchQuery)
    }

    fun query(getSql: (queryBuilder: QueryBuilder) -> String): Int {
        val query = this.ser.selectQuery(getSql = getSql)
        return this.exe.update(query = query)
    }

    fun <OUT : Any> query(output: KClass<OUT>, getSql: (queryBuilder: QueryBuilderOut<OUT>) -> String): List<OUT> {
        val query = this.ser.selectQuery(output = output, getSql = getSql)
        return this.exe.query(query = query) {
            this.ser.repo.decode(resultSet = it, kclass = output)
        }
    }

    fun <IN : Any, OUT : Any> query(input: IN, output: KClass<OUT>, getSql: (queryBuilder: QueryBuilderInOut<IN, OUT>) -> String): List<OUT> {
        val query = this.ser.selectQuery(input = input, output = output, getSql = getSql)
        return this.exe.query(query = query) {
            this.ser.repo.decode(resultSet = it, kclass = output)
        }
    }

    fun <IN : Any, OUT : Any> call(input: IN, output: KClass<OUT>): List<OUT> {
        val query = this.ser.callQuery(input = input)
        return this.exe.query(query = query) {
            this.ser.repo.decode(resultSet = it, kclass = output)
        }
    }
}
