package com.urosjarc.dbmessiah.impl.postgresql

import com.urosjarc.dbmessiah.QueryConnection
import com.urosjarc.dbmessiah.domain.queries.Page
import com.urosjarc.dbmessiah.domain.queries.QueryBuilder
import java.sql.Connection
import kotlin.reflect.KClass

open class PgQueryConn(conn: Connection, ser: PgSerializer) {

    val conn = QueryConnection(conn = conn, ser = ser)

    /**
     * TABLE
     */
    fun <T : Any> drop(table: KClass<T>, cascade: Boolean = false): Int = this.conn.drop(table = table, cascade = cascade)
    fun <T : Any> create(table: KClass<T>): Int = this.conn.create(table = table)
    fun <T : Any> delete(table: KClass<T>): Int = this.conn.delete(table = table)
    fun <T : Any> select(table: KClass<T>): List<T> = this.conn.select(table = table)
    fun <T : Any> select(table: KClass<T>, page: Page<T>): List<T> = this.conn.select(table = table, page = page)

    /**
     * ROW
     */
    fun <T : Any, K : Any> select(table: KClass<T>, pk: K): T? = this.conn.select(table = table, pk = pk)
    fun <T : Any> insert(row: T): Boolean = this.conn.insert(row = row)
    fun <T : Any> update(row: T): Boolean = this.conn.update(row = row)
    fun <T : Any> delete(row: T): Boolean = this.conn.delete(row = row)

    /**
     * ROWS
     */
    fun <T : Any> insert(rows: Iterable<T>): List<Boolean> = this.conn.insert(rows = rows)
    fun <T : Any> update(rows: Iterable<T>): List<Boolean> = this.conn.update(rows = rows)
    fun <T : Any> delete(rows: Iterable<T>): List<Boolean> = this.conn.delete(rows = rows)


    /**
     * BATCH ROWS
     */
    fun <T : Any> insertBatch(rows: Iterable<T>): Int = this.conn.insertBatch(rows = rows)
    fun <T : Any> updateBatch(rows: Iterable<T>): Int = this.conn.updateBatch(rows = rows)
    fun <T : Any> deleteBatch(rows: Iterable<T>): Int = this.conn.deleteBatch(rows = rows)

    /**
     * PROCEDURES
     */
    fun <IN : Any> call(function: IN, vararg outputs: KClass<*>): List<List<Any>> = this.conn.call(procedure = function, outputs = outputs)

    /**
     * QUERY
     */
    fun query(getSql: () -> String) = this.conn.query(getSql = getSql)

    fun query(vararg outputs: KClass<*>, getSql: () -> String): List<List<Any>> = this.conn.query(outputs = outputs, getSql = getSql)

    fun <IN : Any> query(vararg outputs: KClass<*>, input: IN, getSql: (queryBuilder: QueryBuilder<IN>) -> String): List<List<Any>> =
        this.conn.query(outputs = outputs, input = input, getSql = getSql)

}
