package com.urosjarc.dbmessiah.domain.call

import com.urosjarc.dbmessiah.domain.querie.QueryValue
import kotlin.reflect.KClass

data class Procedure(
    val schema: String?,
    val kclass: KClass<*>,
    val args: List<ProcedureArg>,
) {
    init {
        //Init late init parent connection
        this.args.forEach { it.procedure = this }
    }

    val name: String = kclass.simpleName.toString()
    val path: String = listOf(this.schema, this.name).joinToString(".")
    val hash = this.path.hashCode()
    override fun hashCode(): Int = this.hash
    override fun equals(other: Any?): Boolean =
        this.hashCode() == other.hashCode()

    override fun toString(): String {
        val argsNames = this.args.joinToString(", ") { "${it.name}: ${it.kclass.simpleName}" }
        return "${kclass.simpleName}($argsNames)"
    }

    fun sqlArguments(separator: String = ", "): String =
        this.args.joinToString(separator = separator) { "?" }

    fun queryValues(obj: Any): Array<out QueryValue> = this.args
        .map { QueryValue(name = it.name, value = it.getValue(obj = obj), jdbcType = it.jdbcType, encoder = it.encoder) }
        .toTypedArray()

}
