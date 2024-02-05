package com.urosjarc.dbmessiah.domain.table

import com.urosjarc.dbmessiah.domain.columns.Column
import com.urosjarc.dbmessiah.domain.columns.ForeignColumn
import com.urosjarc.dbmessiah.domain.columns.OtherColumn
import com.urosjarc.dbmessiah.domain.columns.PrimaryColumn
import com.urosjarc.dbmessiah.domain.querie.QueryValue
import com.urosjarc.dbmessiah.domain.serialization.Encoder
import com.urosjarc.dbmessiah.domain.serialization.TypeSerializer
import java.sql.JDBCType
import kotlin.reflect.KClass

data class TableInfo(
    val schema: String,
    val kclass: KClass<*>,
    val primaryKey: PrimaryColumn,
    val foreignKeys: List<ForeignColumn>,
    val otherColumns: List<OtherColumn>,
    val serializers: List<TypeSerializer<*>>
) {
    val name = this.kclass.simpleName!!
    val path = listOf(this.schema, this.name).joinToString(".")
    val hash = this.path.hashCode()

    init {
        //Init late init parent connection
        (listOf(this.primaryKey) + this.foreignKeys + this.otherColumns).forEach {
            it.table = this
        }
    }

    val userControlledColumns
        get(): List<Column> {
            val columns: MutableList<Column> = (this.foreignKeys + this.otherColumns).toMutableList()
            if (!this.primaryKey.autoIncrement) columns.add(this.primaryKey)
            return columns
        }

    override fun hashCode(): Int = this.hash

    override fun equals(other: Any?): Boolean =
        this.hashCode() == other.hashCode()

    override fun toString(): String = this.path

    fun sqlInsertColumns(separator: String = ", "): String =
        this.userControlledColumns.joinToString(separator = separator) { it.name }

    fun sqlInsertQuestions(separator: String = ", "): String =
        this.userControlledColumns.joinToString(separator = separator) { "?" }

    fun sqlUpdateColumns(separator: String = ", ", zipper: String = " = "): String =
        this.userControlledColumns.joinToString(separator = separator) { it.name + "$zipper?" }

    val jdbcTypes: MutableList<JDBCType> get() = this.userControlledColumns.map { it.jdbcType }.toMutableList()
    val encoders: MutableList<Encoder<*>> get() = userControlledColumns.map { it.encoder }.toMutableList()
    fun queryValues(obj: Any): Array<out QueryValue> = this.userControlledColumns
        .map { QueryValue(name = it.name, value = it.getValue(obj = obj), jdbcType = it.jdbcType, encoder = it.encoder) }
        .toTypedArray()

}
