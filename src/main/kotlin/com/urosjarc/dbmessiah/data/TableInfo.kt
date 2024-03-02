package com.urosjarc.dbmessiah.data

import java.sql.JDBCType
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

/**
 * Represents the metadata of a database table.
 * This class is final representation of database table defined by the user.
 * This class will be used internaly by the system.
 *
 * @param schema The [Schema] that contains this table.
 * @param kclass The class representing the table.
 * @param primaryKey The [PrimaryColumn] for this table.
 * @param foreignKeys The list of [ForeignColumn] for this table.
 * @param otherColumns The list of [OtherColumn] for this table.
 * @param serializers The list of [TypeSerializer] which will help in serialization process.
 */
public data class TableInfo(
    val schema: String,
    val kclass: KClass<*>,
    val primaryKey: PrimaryColumn,
    val foreignKeys: List<ForeignColumn>,
    val otherColumns: List<OtherColumn>,
    val serializers: List<TypeSerializer<*>>
) {

    /**
     * Represents the name of this table.
     */
    val name: String = this.kclass.simpleName!!

    /**
     * Full path where this table is located.
     */
    public val path: String = listOf(this.schema, this.name).joinToString(".")

    /** @suppress */
    private val hash = this.path.hashCode()

    init {
        //Init late init parent connection
        (listOf(this.primaryKey) + this.foreignKeys + this.otherColumns).forEach {
            it.table = this
        }
    }

    public fun getColumn(kprop: KProperty1<*, *>): Column? =
        (listOf(this.primaryKey) + this.foreignKeys + this.otherColumns).firstOrNull { it.kprop == kprop }

    /**
     * Represents a list of columns that can be modified by the user in `UPDATE` statement.
     * This includes the [ForeignColumn] and [OtherColumn], as well as the [PrimaryColumn] if it is not marked auto-increment.
     */
    public val userControlledColumns: List<Column>
        get() {
            val columns: MutableList<Column> = (this.foreignKeys + this.otherColumns).toMutableList()
            if (!this.primaryKey.autoInc) columns.add(0, this.primaryKey)
            return columns
        }

    /**
     * Helper method to generate a SQL string with the column names which should be used in first part of `INSERT` statement.
     * @see [sqlInsertQuestions]
     *
     * @param separator The separator to use between column names. Default is ", ".
     * @return The SQL string with the column names.
     */
    public fun sqlInsertColumns(separator: String = ", ", escaped: (String) -> String): String =
        this.userControlledColumns.joinToString(separator = separator) { escaped(it.name) }

    /**
     * Helper method to generate a SQL string with the values as question marks which should be used in second part of `INSERT` statement.
     * @see [sqlInsertColumns]
     *
     * @param separator The separator to use between question marks. Default is ", ".
     * @return The SQL string with the question marks.
     */
    public fun sqlInsertQuestions(separator: String = ", "): String =
        this.userControlledColumns.joinToString(separator = separator) { "?" }

    /**
     * Helper method to generate a SQL string with the column names and the corresponding update values.
     * This method should be used in `UPDATE` statement.
     *
     * @param separator The separator to use between column names. The default is ", ".
     * @param zipper The string to use between column names and values. The default is " = ".
     * @return The SQL string with the column names and values.
     */
    public fun sqlUpdateColumns(separator: String = ", ", zipper: String = " = ", escaped: (String) -> String): String =
        this.userControlledColumns.joinToString(separator = separator) { escaped(it.name) + "$zipper?" }

    /**
     * Represents a list of JDBC types derived from the [userControlledColumns].
     */
    val jdbcTypes: MutableList<JDBCType> get() = this.userControlledColumns.map { it.jdbcType }.toMutableList()

    /**
     * Represents a list of [Encoder] derived from the [userControlledColumns].
     */
    val encoders: MutableList<Encoder<*>> get() = userControlledColumns.map { it.encoder }.toMutableList()

    /**
     * Extract list of [QueryValue] from the table columns inside [obj].
     * Those [QueryValue] elements will be used further in the system.
     *
     * @param obj The object from which to retrieve the list of [QueryValue].
     * @return An array of [QueryValue] objects representing the values of the object.
     */
    public fun queryValues(obj: Any): Array<out QueryValue> = this.userControlledColumns
        .map { QueryValue(name = it.name, value = it.getValue(obj = obj), jdbcType = it.jdbcType, encoder = it.encoder) }
        .toTypedArray()


    /** @suppress */
    override fun hashCode(): Int = this.hash

    /** @suppress */
    override fun equals(other: Any?): Boolean =
        this.hashCode() == other.hashCode()

    /** @suppress */
    override fun toString(): String = this.path
}
