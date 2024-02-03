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
    fun <T : Any> create(table: KClass<T>): Int = this.conn.create(table = table)
    fun <T : Any> drop(table: KClass<T>, cascade: Boolean = false): Int = this.conn.drop(table = table, cascade = cascade)
    fun <T : Any> delete(table: KClass<T>, cascade: Boolean = false): Int = this.conn.delete(table = table, cascade = cascade)
    fun <T : Any> select(table: KClass<T>): List<T> = this.conn.select(table = table)
    fun <T : Any> select(table: KClass<T>, page: Page<T>): List<T> = this.conn.select(table = table, page = page)

    /**
     * ROWS
     */
    fun <T : Any> insert(row: T): Boolean = this.conn.insert(row = row)
    fun <T : Any> update(row: T): Boolean = this.conn.update(row = row)
    fun <T : Any> delete(row: T, cascade: Boolean = false): Boolean = this.conn.delete(row = row, cascade = cascade)
    fun <T : Any, K : Any> select(table: KClass<T>, pk: K): T? = this.conn.select(table = table, pk = pk)


    /**
     * BATCH ROWS
     */
    fun <T : Any> insert(vararg rows: T): Int = this.conn.insert(rows = rows)
    fun <T : Any> update(vararg rows: T): Int = this.conn.update(rows = rows)
    fun <T : Any> delete(vararg rows: T, cascade: Boolean): Int = this.conn.delete(rows = rows, cascade = cascade)

    /**
     * PROCEDURES
     */
    fun <IN : Any> call(function: IN, vararg outputs: KClass<*>): List<List<Any>?> = this.conn.call(procedure = function, outputs = outputs)

    /**
     * QUERY
     */
    fun query(getSql: () -> String): List<List<Any>?> = this.conn.query(getSql = getSql)

    fun query(vararg outputs: KClass<*>, getSql: () -> String): List<List<Any>?> = this.conn.query(outputs = outputs, getSql = getSql)

    fun <IN : Any> query(vararg outputs: KClass<*>, input: IN, getSql: (queryBuilder: QueryBuilder<IN>) -> String): List<List<Any>?> =
        this.conn.query(outputs = outputs, input = input, getSql = getSql)

}
