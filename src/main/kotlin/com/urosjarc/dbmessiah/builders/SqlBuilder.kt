package com.urosjarc.dbmessiah.builders

import com.urosjarc.dbmessiah.Serializer
import com.urosjarc.dbmessiah.exceptions.QueryException
import com.urosjarc.dbmessiah.extend.ext_owner
import kotlin.reflect.KProperty1


/**
 * An SQL query builder that provides methods for constructing SQL statements.
 * Query builder helps you provide typesafety to final SQL string and to provide user
 * with good user experience while building SQL queries.
 *
 * @param ser The [Serializer] used for escaping and serializing table names, column names, and procedure names.
 */
public open class SqlBuilder(
    public val ser: Serializer
) {
    /**
     * Shortcut for constructing [SELECT] statement for selected table.
     *
     * @return the [SELECT] statement containing full escaped path to specific table.
     */
    public inline fun <reified T : Any> SELECT(): String {
        return "SELECT * FROM ${this.table<T>()}"
    }

    /**
     * Shortcut for constructing [DELETE] statement for selected table.
     *
     * @return the [SELECT] statement containing full escaped path to specific table.
     */
    public inline fun <reified T : Any> DELETE(): String {
        return "DELETE FROM ${this.table<T>()}"
    }

    /**
     * Shortcut for escaping table name.
     *
     * @return The full escaped path to the specified table.
     */
    public inline fun <reified T : Any> table(): String {
        val T = this.ser.mapper.getTableInfo(kclass = T::class)
        return this.ser.escaped(tableInfo = T)
    }

    /**
     * Shortcut for escaping table column name.
     *
     * @return The full escaped path to the specified table column.
     */
    public fun <T : Any> column(kprop: KProperty1<T, *>): String {
        val owner = kprop.ext_owner
        val T = this.ser.mapper.getTableInfo(kclass = owner)
        val C = T.getColumn(kprop = kprop) ?: throw QueryException("Table $T does not have registered column property: $kprop")
        return this.ser.escaped(column = C)
    }

    /**
     * Shortcut for escaping column name.
     *
     * @return The escaped name of specified table column.
     */
    public fun <T : Any> name(column: KProperty1<T, *>): String {
        return ser.escaped(name = column.name)
    }

    /**
     * Shortcut for escaping procedure name.
     *
     * @return The full escaped path to the specified procedure.
     */
    public inline fun <reified T : Any> procedure(): String {
        val P = this.ser.mapper.getProcedure(kclass = T::class)
        return this.ser.escaped(procedure = P)
    }

}
