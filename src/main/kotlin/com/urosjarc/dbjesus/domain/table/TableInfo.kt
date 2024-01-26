package com.urosjarc.dbjesus.domain.table

import com.urosjarc.dbjesus.domain.columns.ForeignColumn
import com.urosjarc.dbjesus.domain.columns.OtherColumn
import com.urosjarc.dbjesus.domain.columns.PrimaryColumn
import com.urosjarc.dbjesus.domain.serialization.Encoder
import com.urosjarc.dbjesus.domain.serialization.TypeSerializer
import java.sql.JDBCType
import kotlin.reflect.KClass

class TableInfo(
    val schema: String,
    val kclass: KClass<*>,
    val primaryKey: PrimaryColumn?,
    val foreignKeys: List<ForeignColumn>,
    val otherColumns: List<OtherColumn>,
    val serializers: List<TypeSerializer<*>>
) {
    val path = "${this.schema}.${this.kclass.simpleName}"
    val allColumns get() = (this.foreignKeys + this.otherColumns).toMutableList().also { if (this.primaryKey != null) it.add(this.primaryKey) }

    fun sqlInsertColumns(escaper: String = "'", separator: String = ", "): String =
        this.allColumns.joinToString(separator = separator) { "$escaper${it.name}$escaper" }

    fun sqlInsertQuestions(separator: String = ", "): String =
        this.allColumns.joinToString(separator = separator) { "?" }

    fun sqlUpdateColumns(escaper: String = "'", separator: String = ", ", zipper: String = " = "): String =
        this.allColumns.joinToString(separator = separator) { "$escaper${it.name}$escaper$zipper?" }

    val values: MutableList<Any?> get() = this.allColumns.map { it.value }.toMutableList()
    val jdbcTypes: MutableList<JDBCType> get() = this.allColumns.map { it.jdbcType }.toMutableList()
    val encoders: MutableList<Encoder<*>> get() = allColumns.map { it.encoder }.toMutableList()

}
