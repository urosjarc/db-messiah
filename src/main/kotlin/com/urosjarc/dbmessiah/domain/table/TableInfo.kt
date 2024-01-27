package com.urosjarc.dbmessiah.domain.table

import com.urosjarc.dbmessiah.domain.columns.Column
import com.urosjarc.dbmessiah.domain.columns.ForeignColumn
import com.urosjarc.dbmessiah.domain.columns.OtherColumn
import com.urosjarc.dbmessiah.domain.columns.PrimaryColumn
import com.urosjarc.dbmessiah.domain.queries.QueryValue
import com.urosjarc.dbmessiah.domain.serialization.Encoder
import com.urosjarc.dbmessiah.domain.serialization.TypeSerializer
import java.sql.JDBCType
import kotlin.reflect.KClass

data class TableInfo(
    val escaper: String,
    val schema: String,
    val kclass: KClass<*>,
    val primaryKey: PrimaryColumn,
    val foreignKeys: List<ForeignColumn>,
    val otherColumns: List<OtherColumn>,
    val serializers: List<TypeSerializer<*>>
) {
    val name = this.kclass.simpleName!!
    val cleanPath = "${this.schema}.${this.name}"
    val path = "$escaper${this.schema}$escaper.$escaper${this.name}$escaper"
    val userControlledColumns
        get(): List<Column> {
            val columns: MutableList<Column> = (this.foreignKeys + this.otherColumns).toMutableList()
            if (!this.primaryKey.autoIncrement) columns.add(this.primaryKey)
            return columns
        }

    override fun hashCode(): Int {
        return this.cleanPath.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        return this.hashCode() == other.hashCode()
    }

    override fun toString(): String {
        return this.cleanPath
    }

    fun sqlInsertColumns(separator: String = ", "): String =
        this.userControlledColumns.joinToString(separator = separator) { "$escaper${it.name}$escaper" }

    fun sqlInsertQuestions(separator: String = ", "): String =
        this.userControlledColumns.joinToString(separator = separator) { "?" }

    fun sqlUpdateColumns(separator: String = ", ", zipper: String = " = "): String =
        this.userControlledColumns.joinToString(separator = separator) { "$escaper${it.name}$escaper$zipper?" }

    val jdbcTypes: MutableList<JDBCType> get() = this.userControlledColumns.map { it.jdbcType }.toMutableList()
    val encoders: MutableList<Encoder<*>> get() = userControlledColumns.map { it.encoder }.toMutableList()
    fun values(obj: Any): List<QueryValue> {
        return this.userControlledColumns.map { QueryValue(name = it.name, value = it.value(obj = obj), jdbcType = it.jdbcType, encoder = it.encoder) }
    }

}
