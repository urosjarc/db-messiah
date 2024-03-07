package com.urosjarc.dbmessiah.data

import com.urosjarc.dbmessiah.builders.RowBuilder
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

/**
 * Represents the database table.
 * This class is final representation of database table defined by the user.
 * This class will be used internally by the system.
 *
 * @param schema The schema that contains this table.
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
     * Represents the unescaped table name.
     */
    val name: String = this.kclass.simpleName!!

    /**
     * Full path where this table is located.
     */
    public val path: String = listOf(this.schema, this.name).joinToString(".")

    /**
     * Assign [TableInfo] instance to all their children.
     */
    init {
        (listOf(this.primaryKey) + this.foreignKeys + this.otherColumns).forEach {
            it.table = this
        }
    }

    /**
     * Retrieves the [Column] corresponding to the given [KProperty1].
     *
     * @param kprop The [KProperty1] representing the property.
     * @return The corresponding [Column] or null if not found.
     */
    public fun getColumn(kprop: KProperty1<*, *>): Column? =
        (listOf(this.primaryKey) + this.foreignKeys + this.otherColumns).firstOrNull { it.kprop == kprop }

    /**
     * Retrieves a list of columns for the table with optional primary key included.
     *
     * @param withPrimaryColumn Boolean flag indicating whether the primary key should be included in the list of columns.
     * @return A List of Column objects representing the columns in the table.
     */
    public fun getRowBuilder(withPrimaryColumn: Boolean): RowBuilder {
        val columns: MutableList<Column> = (foreignKeys + otherColumns).toMutableList()
        if (withPrimaryColumn) columns.add(0, this.primaryKey)
        return RowBuilder(columns = columns)
    }

    /** @suppress */
    override fun hashCode(): Int = path.hashCode()//OK

    /** @suppress */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as TableInfo
        return path == other.path
    }

    /** @suppress */
    override fun toString(): String = this.path
}
