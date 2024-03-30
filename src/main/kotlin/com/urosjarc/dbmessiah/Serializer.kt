package com.urosjarc.dbmessiah

import com.urosjarc.dbmessiah.builders.QueryBuilder
import com.urosjarc.dbmessiah.builders.SqlBuilder
import com.urosjarc.dbmessiah.data.*
import com.urosjarc.dbmessiah.domain.Cursor
import com.urosjarc.dbmessiah.domain.Order
import com.urosjarc.dbmessiah.domain.Page
import com.urosjarc.dbmessiah.extend.ext_kprops
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

/**
 * Serializer is an abstract class that provides functionality for serializing objects to various string like formats.
 * This class should be implemented and overriden by specific database implementation.
 * On class initialization all internal structure provided by the user will be heavily tested for any
 * inconsistency or error provided by the user mistake. These tests are trying to provide security, reliability and
 * ultimately trust to the end user.
 *
 * @param allowAutoUUID A [Boolean] flag representing if database can have AUTO_UUID as primary keys.
 * @param schemas A list of [Schema] objects representing the database schemas that the serializer will work with.
 * @param globalSerializers A list of [TypeSerializer] objects representing global serializers to be used by the serializer.
 * @param globalInputs A list of [KClass] objects representing global input classes that can be used by the serializer.
 * @param globalOutputs A list of [KClass] objects representing global output classes that can be used by the serializer.
 * @param globalProcedures A list of [KClass] objects representing global procedures that can be used by the serializer.
 */
public abstract class Serializer(
    internal val allowAutoUUID: Boolean,
    final override val schemas: List<Schema>,
    internal val globalSerializers: List<TypeSerializer<*>>,
    internal val globalInputs: List<KClass<*>> = listOf(),
    internal val globalOutputs: List<KClass<*>> = listOf(),
    internal val globalProcedures: List<KClass<*>> = listOf()
): Exporter(schemas = schemas) {

    init {
        SerializerTests.EmptinessTests(ser = this).also {
            it.`At least one table must exist`()
            it.`Global inputs must have at least 1 property and primary constructor with required non-optional parameters`()
            it.`Global outputs must have at least 1 property and primary constructor with required non-optional parameters`()
            it.`Global procedures must have primary constructor with non-optional parameters`()
            it.`Schemas procedures must have primary constructor with non-optional parameters`()
            it.`Tables must have non-empty primary constructor and columns`()
        }
        SerializerTests.UniquenessTests(ser = this).also {
            it.`Schemas must be unique`()
            it.`Global serializers must be unique`()
            it.`Global inputs must be unique`()
            it.`Global outputs must be unique`()
            it.`Global procedures must be unique`()
            it.`Schemas serializers must be unique`()
            it.`Schemas procedures must be unique`()
            it.`Tables must be unique`()
            it.`Tables serializers keys must be unique`()
            it.`Column constraints must be unique`()
        }
        SerializerTests.NamingTests(ser = this).also {
            it.`Schema must have valid name`()
            it.`Global outputs must have valid name with valid property and parameter names`()
            it.`Global inputs must have valid name with valid property and parameter names`()
            it.`Global procedures must have valid name with valid property and parameter names`()
            it.`Schemas tables must have valid name with valid property and parameter names`()
            it.`Schemas procedures must have valid name with valid property and parameter names`()
        }
        SerializerTests.TableTests(ser = this).also {
            it.`Tables primary keys can be AUTO_UUID if database allows it`()
            it.`Tables primary keys must not be imutable and optional at the same time`()
            it.`Tables primary keys must not be mutable and not optional at the same time`()
            it.`Tables primary keys can be only be whole number or UUID`()
            it.`Tables foreign keys must not contain primary key`()
            it.`Tables foreign keys must point to registered table with primary key of same type`()
            it.`Tables constraints must be valid for specific column`()
        }
        SerializerTests.SerializationTests(ser = this).also {
            it.`All database values must have excactly one matching type serializer`()
        }
    }

    /**
     * Variable representing an instance of the [Mapper] class.
     * This instance will help us map object from database to user space and back.
     *
     * @property mapper The instance of the [Mapper] class.
     */
    public val mapper: Mapper = Mapper(
        schemas = schemas.toList(),
        globalSerializers = globalSerializers,
        globalInputs = globalInputs,
        globalOutputs = globalOutputs,
        globalProcedures = globalProcedures
    )


    /**
     * Represents the SQL statement that will retrieve last id from database by force.
     *
     * @property selectLastId The last id selected from the database.
     */
    public open var selectLastId: String? = null

    /**
     * Escapes the given string with database specific escaping quotation.
     *
     * @param name The string to be escaped.
     * @return The escaped string.
     */
    public abstract fun escaped(name: String): String

    /**
     * Escapes the given [Schema] name with database specific escaping quotation.
     *
     * @param name The string to be escaped.
     * @return The escaped string.
     */
    public fun escaped(schema: Schema): String = escaped(name = schema.name)

    /**
     * Escapes the given [Procedure] path with database specific escaping quotation.
     *
     * @param procedure The [Procedure] object representing the procedure.
     * @return The escaped string.
     */
    public fun escaped(procedure: Procedure): String =
        listOf(procedure.schema, procedure.name).filterNotNull().joinToString(".") { escaped(name = it) }

    /**
     * Escapes the given procedure argument name with database specific escaping quotation.
     *
     * @param procedureArg The property representing the column to be escaped.
     * @return The escaped string.
     */
    public open fun <T : Any> escaped(procedureArg: KProperty1<T, *>): String = escaped(name = procedureArg.name)

    /**
     * Escapes the given table path with database specific escaping quotation.
     *
     * @param tableInfo The TableInfo object representing the table information.
     * @return The escaped string representation of the table info.
     */
    public fun escaped(tableInfo: TableInfo): String = listOf(tableInfo.schema, tableInfo.name).joinToString(".") { escaped(name = it) }

    /**
     * Escapes the given column name with database specific escaping quotation.
     *
     * @param column The column whose name needs to be escaped.
     * @return The escaped column name.
     */
    public fun escaped(column: Column): String =
        listOf(column.table.schema, column.table.name, column.name).joinToString(".") { escaped(name = it) }

    /**
     * Creates [Query] for creating a schema in the database.
     *
     * @param schema The schema object representing the schema to be created.
     * @return The [String] to create the schema.
     */
    public open fun createSchema(schema: Schema): Query = Query(sql = "CREATE SCHEMA IF NOT EXISTS ${escaped(schema)}")

    /**
     * Creates [Query] for dropping specific schema.
     *
     * @param schema The schema to be dropped.
     * @return The [String] representing the drop query.
     */
    public open fun dropSchema(schema: Schema, cascade: Boolean = false): Query {
        val cascadeSql = if (cascade) " CASCADE" else ""
        return Query(sql = "DROP SCHEMA IF EXISTS ${escaped(schema)}$cascadeSql")
    }

    /**
     * Makes [Query] for dropping a database table.
     *
     * @param table The Kotlin class representing the table to be dropped.
     * @param cascade Flag indicating whether to drop the table cascade (i.e., also drop dependent objects).
     * @return The [Query] object representing the drop query.
     */
    public open fun <T : Any> dropTable(table: KClass<T>, cascade: Boolean = false): Query {
        val T = this.mapper.getTableInfo(kclass = table)
        val cascadeSql = if (cascade) " CASCADE" else ""
        return Query(sql = "DROP TABLE IF EXISTS ${escaped(T)}$cascadeSql")
    }

    /**
     * Makes [Query] for creating new table.
     *
     * @param table The Kotlin class representing the database table to which the row will be created.
     * @return The [Query] object representing the SQL query.
     */
    public abstract fun <T : Any> createTable(table: KClass<T>): Query

    /**
     * Makes [Query] for deleting all rows from the specified table.
     *
     * @param table The Kotlin class representing the table to delete records from.
     * @return The [Query] object representing the delete query.
     */
    public fun <T : Any> deleteTable(table: KClass<T>): Query {
        val T = this.mapper.getTableInfo(kclass = table)
        return Query(sql = "DELETE FROM ${escaped(T)}")
    }

    /**
     * Makes [Query] for deleting specific row by id from the database table.
     *
     * @param row The object representing the table from which row to be deleted.
     * @return The [Query] object representing the delete query.
     */
    public fun deleteRow(row: Any): Query {
        val T = this.mapper.getTableInfo(obj = row)
        return Query(
            sql = "DELETE FROM ${escaped(T)} WHERE ${escaped(T.primaryColumn)} = ?",
            T.primaryColumn.queryValueFrom(row)
        )
    }

    /**
     * Makes [Query] for inserting new row.
     *
     * @param row The object to be inserted into the database.
     * @param batch A flag indicating whether to use batch insertion. Default is false.
     * @return The [Query] object representing the SQL insert query.
     */
    public open fun insertRow(row: Any, batch: Boolean): Query {
        val T = this.mapper.getTableInfo(obj = row)
        val RB = T.getInsertRowBuilder()
        val escapedColumns = RB.sqlColumns { escaped(it) }
        return Query(
            sql = "INSERT INTO ${escaped(T)} ($escapedColumns) VALUES (${RB.sqlQuestions()})",
            *RB.queryValues(obj = row),
        )
    }

    /**
     * Makes [Query] for updating row by specific id in the database table.
     *
     * @param row The object representing the row to be updated.
     * @return The [Query] object representing the SQL update query.
     */
    public fun updateRow(row: Any): Query {
        val T = this.mapper.getTableInfo(obj = row)
        val RB = T.getUpdateRowBuilder()
        val escapedColumns = RB.sqlColumns { "${escaped(it)} = ?" }
        return Query(
            sql = "UPDATE ${escaped(T)} SET $escapedColumns WHERE ${escaped(T.primaryColumn)} = ?",
            *RB.queryValues(obj = row),
            T.primaryColumn.queryValueFrom(obj = row)
        )
    }

    /**
     * Makes [Query] for selecting all rows from table.
     *
     * @param table The Kotlin class or table to query.
     * @return The Query object representing the SELECT query.
     */
    public fun <T : Any> selectTable(table: KClass<T>): Query {
        val T = this.mapper.getTableInfo(kclass = table)
        return Query(sql = "SELECT * FROM ${escaped(T)}")
    }

    /**
     * Makes [Query] for selecting rows from table with offset pagination.
     *
     * @param table the Kotlin class to query from.
     * @param page the pagination details.
     * @return a Query object representing the executed database query.
     */
    public open fun <T : Any> selectTable(table: KClass<T>, page: Page<T>): Query {
        val T = this.mapper.getTableInfo(kclass = table)
        return Query(sql = "SELECT * FROM ${escaped(T)} ORDER BY ${escaped(page.orderBy.name)} ${page.order} LIMIT ${page.limit} OFFSET ${page.offset}")
    }

    /**
     * Makes [Query] for selecting rows from table with cursor pagination.
     *
     * @param table the Kotlin class representing the table to be queried
     * @param cursor the cursor object containing query parameters such as order by, order and limit
     * @return a Query object representing the executed query
     */
    public open fun <T : Any, V : Comparable<V>> selectTable(table: KClass<T>, cursor: Cursor<T, V>): Query {
        val T = this.mapper.getTableInfo(kclass = table)
        val lge = when (cursor.order) {
            Order.ASC -> ">="
            Order.DESC -> "<="
        }
        return Query(sql = "SELECT * FROM ${escaped(T)} WHERE ${escaped(cursor.orderBy.name)} $lge ${cursor.index} ORDER BY ${escaped(cursor.orderBy.name)} ${cursor.order} LIMIT ${cursor.limit}")
    }

    /**
     * Makes [Query] for a specific row based on the primary key value.
     *
     * @param table The class representing the table in the database.
     * @param pk The primary key value of the record to query.
     * @return A `Query` object representing the SQL query to fetch the record.
     */
    public fun <T : Any, K : Any> selectTable(table: KClass<T>, pk: K): Query {
        val T = this.mapper.getTableInfo(kclass = table)
        val qv = T.primaryColumn.queryValue(value = pk)
        // qv must be templated and escaped like so because user can use inlined primary key!
        return Query(sql = "SELECT * FROM ${escaped(T)} WHERE ${escaped(T.primaryColumn)} = ${qv.escapped}")
    }


    /**
     * Makes [Query] from user defined SQL string
     *
     * @param buildSql A function that returns the user provided SQL statement to be executed.
     * @return A [Query] object representing the SQL query and its values.
     */
    public fun query(buildSql: (SqlBuilder) -> String): Query = Query(sql = buildSql(SqlBuilder(ser = this)))

    /**
     * Creates [Query] from user defined SQL string with inputs.
     *
     * @param input The input object used to generate the query.
     * @param buildSql A function that takes a QueryBuilder and returns the SQL string for the query.
     * @return A Query object representing the generated query.
     */
    public fun <IN : Any> queryWithInput(input: IN, buildSql: (queryBuilder: QueryBuilder<IN>) -> String): Query {
        val qBuilder = QueryBuilder(input = input, ser = this)
        return qBuilder.build(sql = buildSql(qBuilder))
    }

    /**
     * Makes [Query] for creating a stored procedure in the database.
     *
     * @param procedure The Kotlin class representing the stored procedure.
     * @param procedureBody The string representing the body of the stored procedure.
     * @return A [Query] object representing the SQL query to create the stored procedure.
     */
    public abstract fun <T : Any> createProcedure(procedure: KClass<T>, procedureBody: String): Query

    /**
     * Makes [Query] for calling stored procedure.
     *
     * @param procedure The stored procedure object to be executed.
     * @return A [Query] object representing the executed query.
     */
    public abstract fun <T : Any> callProcedure(procedure: T): Query

    /**
     * Makes [Query] for dropping a stored procedure from the database.
     *
     * @param procedure The Kotlin class representing the stored procedure to be dropped.
     * @return A [Query] object representing the SQL query to drop the stored procedure.
     */
    public abstract fun <T : Any> dropProcedure(procedure: KClass<T>): Query

}
