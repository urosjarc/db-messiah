package com.urosjarc.dbmessiah.builders

import com.urosjarc.dbmessiah.data.Column
import com.urosjarc.dbmessiah.data.QueryValue

/**
 * The RowBuilder class is responsible for generating SQL strings and query values based on a list of columns.
 *
 * @param columns The list of columns.
 */
public class RowBuilder(
    private val columns: List<Column>
) {

    /**
     * Helper method to generate a SQL string with the column names which should be used in first part of `INSERT` statement.
     * @see [sqlInsertQuestions]
     *
     * @param separator The separator to use between column names. Default is ", ".
     * @return The SQL string with the column names.
     */
    public fun sqlInsertColumns(separator: String = ", ", escaped: (String) -> String): String =
        this.columns.joinToString(separator = separator) { escaped(it.name) }

    /**
     * Helper method to generate a SQL string with the values as question marks which should be used in second part of `INSERT` statement.
     * @see [sqlInsertColumns]
     *
     * @param separator The separator to use between question marks. Default is ", ".
     * @return The SQL string with the question marks.
     */
    public fun sqlInsertQuestions(separator: String = ", "): String =
        this.columns.joinToString(separator = separator) { "?" }

    /**
     * Helper method to generate a SQL string with the column names and the corresponding update values.
     * This method should be used in `UPDATE` statement.
     *
     * @param separator The separator to use between column names. The default is ", ".
     * @param zipper The string to use between column names and values. The default is " = ".
     * @return The SQL string with the column names and values.
     */
    public fun sqlUpdateColumns(separator: String = ", ", zipper: String = " = ", escaped: (String) -> String): String =
        this.columns.joinToString(separator = separator) { escaped(it.name) + "$zipper?" }

    /**
     * Extract list of [QueryValue] from the table columns inside [obj].
     * Those [QueryValue] elements will be used further in the system.
     *
     * @param obj The object from which to retrieve the list of [QueryValue].
     * @return An array of [QueryValue] objects representing the values of the object.
     */
    public fun queryValues(obj: Any): Array<out QueryValue> = this.columns
        .map { QueryValue(name = it.name, value = it.getValue(obj = obj), jdbcType = it.jdbcType, encoder = it.encoder) }
        .toTypedArray()
}
