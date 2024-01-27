package com.urosjarc.dbmessiah.domain.table

import com.urosjarc.dbmessiah.domain.columns.ForeignColumn
import com.urosjarc.dbmessiah.domain.columns.OtherColumn
import com.urosjarc.dbmessiah.domain.columns.PrimaryColumn
import com.urosjarc.dbmessiah.domain.queries.QueryValue
import com.urosjarc.dbmessiah.domain.serialization.Encoder
import com.urosjarc.dbmessiah.domain.serialization.TypeSerializer
import java.sql.JDBCType
import kotlin.reflect.KClass

class TableInfo(
    val escaper: String,
    val schema: String,
    val kclass: KClass<*>,
    val primaryKey: PrimaryColumn,
    val foreignKeys: List<ForeignColumn>,
    val otherColumns: List<OtherColumn>,
    val serializers: List<TypeSerializer<*>>
) {
    val name = this.kclass.simpleName!!

    val path = "$escaper${this.schema}$escaper.$escaper${this.name}$escaper"
    val allColumns get() = this.foreignKeys + this.otherColumns

    fun sqlInsertColumns(separator: String = ", "): String =
        this.allColumns.joinToString(separator = separator) { "$escaper${it.name}$escaper" }

    fun sqlInsertQuestions(separator: String = ", "): String =
        this.allColumns.joinToString(separator = separator) { "?" }

    fun sqlUpdateColumns(separator: String = ", ", zipper: String = " = "): String =
        this.allColumns.joinToString(separator = separator) { "$escaper${it.name}$escaper$zipper?" }

    val jdbcTypes: MutableList<JDBCType> get() = this.allColumns.map { it.jdbcType }.toMutableList()
    val encoders: MutableList<Encoder<*>> get() = allColumns.map { it.encoder }.toMutableList()
    fun values(obj: Any): List<QueryValue> {
        return this.allColumns.map { QueryValue(name = it.name, value = it.value(obj = obj), jdbcType = it.jdbcType, encoder = it.encoder) }
    }
}
