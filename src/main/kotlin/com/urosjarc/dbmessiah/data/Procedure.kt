package com.urosjarc.dbmessiah.data

import kotlin.reflect.KClass

/**
 * Represents a database procedure.
 *
 * @property schema The schema where this procedure is located.
 * @property kclass The Kotlin class representing the procedure.
 * @property args The list of [ProcedureArg] representing procedure arguments.
 */
internal data class Procedure(
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
     * Represents the full path of a [Procedure].
     */
    val path: String = listOf(this.schema, this.name).filterNotNull().joinToString(".")

    /**
     * Generates a string representing the SQL arguments as question marks.
     * The string will be used in SQL query as template for JDBC.
     *
     * @param separator The separator to use between argument placeholders. Defaults to ", ".
     * @return A string containing the SQL arguments. Example: `?, ?, ?`.
     */
    fun sqlArguments(separator: String = ", "): String =
        this.args.joinToString(separator = separator) { "?" }

    /**
     * Important function that extract values from object representing [Procedure] and maps them to [QueryValue].
     * Those [QueryValue] will be further used by the system.
     *
     * @param obj The object to query values from.
     * @return An array of [QueryValue] objects representing extracted values from the object.
     */
    fun queryValues(obj: Any): Array<out QueryValue> = this.args
        .map { QueryValue(name = it.name, value = it.getValue(obj = obj), jdbcType = it.jdbcType, encoder = it.encoder) }
        .toTypedArray()

    private val hash = this.path.hashCode()
    override fun hashCode(): Int = this.hash
    override fun equals(other: Any?): Boolean =
        this.hashCode() == other.hashCode()

    override fun toString(): String {
        val argsNames = this.args.joinToString(", ") { "${it.name}: ${it.kclass.simpleName}" }
        return "${kclass.simpleName}($argsNames)"
    }
}
