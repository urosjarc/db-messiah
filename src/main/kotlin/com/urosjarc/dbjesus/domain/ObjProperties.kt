package com.urosjarc.dbjesus.domain

import java.sql.PreparedStatement

class ObjProperties(
    val primaryKey: ObjProperty,
    val list: MutableList<ObjProperty> = mutableListOf(),
) {
    fun find(name: String): ObjProperty? =
        this.list.firstOrNull { it.name == name }

    fun sqlInsertColumns(escaper: String = "'", separator: String = ", "): String =
        this.list.joinToString(separator = separator) { "$escaper${it.name}$escaper" }

    fun sqlInsertValues(separator: String = ", ", questionMark: String = "?"): String =
        this.list.joinToString(separator = separator) { questionMark }

    fun sqlUpdate(escaper: String = "'", separator: String = ", ", zipper: String = " = ", questionMark: String = "?"): String =
        this.list.joinToString(separator = separator) { "$escaper${it.name}$escaper$zipper$questionMark" }

    val encoders: MutableList<(ps: PreparedStatement, i: Int, x: Any, eInfo: EncodeInfo) -> Unit> get() = list.map { it.encoder }.toMutableList()
}
