package com.urosjarc.dbmessiah.domain.call

import com.urosjarc.dbmessiah.domain.columns.Column
import com.urosjarc.dbmessiah.domain.queries.QueryValue
import com.urosjarc.dbmessiah.domain.table.Escaper
import kotlin.reflect.KClass

data class Procedure(
    val escaper: Escaper,
    val kclass: KClass<*>,
    val args: List<ProcedureArg>,
) {
    val name: String = kclass.simpleName.toString()
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
