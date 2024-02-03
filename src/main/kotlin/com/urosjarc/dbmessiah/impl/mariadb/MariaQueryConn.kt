package com.urosjarc.dbmessiah.impl.mariadb

import com.urosjarc.dbmessiah.QueryConnection
import com.urosjarc.dbmessiah.domain.queries.Page
import com.urosjarc.dbmessiah.domain.queries.QueryBuilder
import java.sql.Connection
import kotlin.reflect.KClass

open class MariaQueryConn(conn: Connection, ser: MariaSerializer) {

    val conn = QueryConnection(conn = conn, ser = ser)

    /**
     * TABLE
     */
    fun <T : Any> drop(table: KClass<T>): Int = this.conn.drop(table = table)
    fun <T : Any> create(table: KClass<T>): Int = this.conn.create(table = table)
    fun <T : Any> delete(table: KClass<T>): Int = this.conn.delete(table = table)

    fun <T : Any> select(table: KClass<T>): List<T> = this.conn.select(table = table)
    fun <T : Any> select(table: KClass<T>, page: Page<T>): List<T> = this.conn.select(table = table, page = page)

    /**
     * ROWS
     */
    fun <T : Any> insert(row: T): Boolean = this.conn.insert(row = row)
    fun <T : Any> update(row: T): Boolean = this.conn.update(row = row)
    fun <T : Any> delete(row: T): Boolean = this.conn.delete(row = row)
    fun <T : Any, K : Any> select(table: KClass<T>, pk: K): T? = this.conn.select(table = table, pk = pk)


    /**
     * BATCH ROWS
     */
    fun <T : Any> insert(vararg rows: T): Int = this.conn.insert(rows = rows)
    fun <T : Any> update(vararg rows: T): Int = this.conn.update(rows = rows)
    fun <T : Any> delete(vararg rows: T): Int = this.conn.delete(rows = rows)

    /**
     * PROCEDURES
     */
    fun <IN : Any> call(procedure: IN, vararg outputs: KClass<*>): List<List<Any>?> =
        this.conn.call(procedure = procedure, outputs = outputs)

    /**
     * QUERY
     */
    fun query(getSql: () -> String): List<Any>? =
        this.conn.query(getSql = getSql).firstOrNull()

    fun query(vararg outputs: KClass<*>, getSql: () -> String): List<Any>? =
        this.conn.query(outputs = outputs, getSql = getSql).firstOrNull()

    fun <IN : Any> query(vararg outputs: KClass<*>, input: IN, getSql: (queryBuilder: QueryBuilder<IN>) -> String): List<Any>? =
        this.conn.query(outputs = outputs, input = input, getSql = getSql).firstOrNull()

}
