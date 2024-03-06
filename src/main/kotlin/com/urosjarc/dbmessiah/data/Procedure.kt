package com.urosjarc.dbmessiah.data

import kotlin.reflect.KClass

/**
 * Represents a database procedure.
 *
 * @property schema The schema in which this [Procedure] is located.
 * @property kclass Class representing this [Procedure].
 * @property args The list of procedure arguments.
 */
public data class Procedure(
    val schema: String?,
    val kclass: KClass<*>,
    val args: List<ProcedureArg>,
) {
    init {
        //Init late init parent connection
        this.args.forEach { it.procedure = this }
    }

    /**
     * Represents the name of the [Procedure].
     */
    val name: String = kclass.simpleName.toString()

    /**
     * Represents the full path of a [Procedure] location.
     */
    val path: String = listOf(this.schema, this.name).filterNotNull().joinToString(".")

    /**
     * Generates a string representing the SQL arguments as question marks.
     * The string will be used in SQL query as template for JDBC.
     *
     * @param separator The separator to use between argument placeholders. Defaults to ", ".
     * @return A string containing the SQL arguments. Example: `?, ?, ?`.
     */
    public fun sqlArguments(separator: String = ", "): String =
        this.args.joinToString(separator = separator) { "?" }

    /**
     * Important function that extract values from object representing [Procedure] and maps them to [QueryValue].
     * Those [QueryValue] will be further used by the system.
     *
     * @param obj The object to query values from.
     * @return An array of [QueryValue] objects representing extracted values from the object.
     */
    public fun queryValues(obj: Any): Array<out QueryValue> = this.args
        .map { QueryValue(name = it.name, value = it.getValue(obj = obj), jdbcType = it.jdbcType, encoder = it.encoder) }
        .toTypedArray()

    /** @suppress */
    override fun hashCode(): Int = path.hashCode()//OK

    /** @suppress */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as Procedure
        return path == other.path
    }

    /** @suppress */
    override fun toString(): String {
        val argsNames = this.args.joinToString(", ") { "${it.name}: ${it.kclass.simpleName}" }
        return "${kclass.simpleName}($argsNames)"
    }



}
