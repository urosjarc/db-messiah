package com.urosjarc.dbmessiah.builders

import com.urosjarc.dbmessiah.data.*

/**
 * The RowBuilder class is responsible for generating SQL strings and query values based on a list of columns.
 *
 * @param columns The list of columns.
 */
public data class RowBuilder(private val columns: List<Column>) {

    /**
     * Generates a string representation of the columns in SQL format.
     *
     * @param separator The separator to use between columns. Default is ", ".
     * @param escaped A function that escapes the column name.
     * @return A string representation of the columns.
     */
    public fun sqlColumns(separator: String = ", ", escaped: (String) -> String): String =
        this.columns.joinToString(separator = separator) { escaped(it.name) }

    /**
     * Generates a string representation of question marks based on the columns of the RowBuilder instance.
     *
     * @param separator The separator to use between question marks. Default is ", ".
     * @return A string representation of question marks.
     */
    public fun sqlQuestions(separator: String = ", "): String =
        this.columns.joinToString(separator = separator) { "?" }

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
