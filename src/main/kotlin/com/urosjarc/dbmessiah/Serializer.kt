package com.urosjarc.dbmessiah

import com.urosjarc.dbmessiah.data.Query
import com.urosjarc.dbmessiah.data.QueryBuilder
import com.urosjarc.dbmessiah.data.TypeSerializer
import com.urosjarc.dbmessiah.domain.Page
import com.urosjarc.dbmessiah.exceptions.MapperException
import kotlin.reflect.KClass

/**
 * Serializer is an abstract class that provides functionality for serializing objects to various string like formats.
 * This class should be implemented and overriden by specific database implementation.
 *
 * @param schemas A list of [Schema] objects representing the database schemas that the serializer will work with.
 * @param globalSerializers A list of [TypeSerializer] objects representing global serializers to be used by the serializer.
 * @param globalInputs A list of [KClass] objects representing global input classes that can be used by the serializer.
 * @param globalOutputs A list of [KClass] objects representing global output classes that can be used by the serializer.
 * @param globalProcedures A list of [KClass] objects representing global procedures that can be used by the serializer.
 * @property mapper An internal [Mapper] object used for mapping objects to and from the database.
 */
public abstract class Serializer(
    private val schemas: List<Schema>,
    globalSerializers: List<TypeSerializer<*>>,
    globalInputs: List<KClass<*>>,
    globalOutputs: List<KClass<*>>,
    globalProcedures: List<KClass<*>> = listOf()
) {

    internal val mapper = Mapper(
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
    internal open val selectLastId: String? = null


    /**
     * Generates [PlantUML class diagram](https://plantuml.com/class-diagram) representing database arhitecture.
     *
     * @return The PlantUML string for visualizing the database schema.
     */
    public fun plantUML(): String {
        val text = mutableListOf(
            "@startuml",
            "skinparam backgroundColor darkgray",
            "skinparam ClassBackgroundColor lightgray"
        )

        val relationships = mutableMapOf<String, String>()
        val kclass_to_path = mutableMapOf<KClass<*>, String>()
        this.schemas.forEach { s ->
            s.tables.forEach { t ->
                kclass_to_path[t.kclass] = "${s.name}.${t.name}"
            }
        }

        text.add("")
        this.schemas.forEach { s ->
            text.add("package ${s.name} <<Folder>> {")
            s.tables.forEach { t ->
                val className = kclass_to_path[t.kclass]
                val type = (t.primaryKey.returnType.classifier as KClass<*>).simpleName
                text.add("\t class $className {")
                text.add("\t\t ${t.primaryKey.name}: $type")

                t.foreignKeys.forEach {
                    val fk = it.first.name
                    val kclass = it.second
                    val toClass = kclass_to_path[kclass] ?: throw MapperException("Could not find path for kclass: ${kclass.simpleName}.")
                    val fromClass = "${s.name}.${t.name}"
                    relationships[toClass] = fromClass
                    text.add("\t\t $fk: ${kclass.simpleName}")
                }

                text.add("\t }")
            }
            text.add("}")
        }

        text.add("")
        relationships.forEach { t, u ->
            text.add("$t -down-> $u")
        }

        text.add("")
        text.add("@enduml")

        return text.joinToString("\n")

    }

    /**
     * Creates a SQL string to create a schema in the database.
     *
     * @param schema The schema object representing the schema to be created.
     * @return The [String] to create the schema.
     */
    internal fun createQuery(schema: Schema) = Query(sql = "CREATE SCHEMA IF NOT EXISTS ${schema.name}")

    /**
     * Creates a SQL string representing a drop query for the given schema.
     *
     * @param schema The schema to be dropped.
     * @return The [String] representing the drop query.
     */
    internal fun dropQuery(schema: Schema) = Query(sql = "DROP SCHEMA IF EXISTS ${schema.name}")

    /**
     * Creates a SQL string to drop a database table.
     *
     * @param kclass The Kotlin class representing the table to be dropped.
     * @param cascade Flag indicating whether to drop the table cascade (i.e., also drop dependent objects).
     * @return The [Query] object representing the drop query.
     */
    internal open fun <T : Any> dropQuery(kclass: KClass<T>, cascade: Boolean = false): Query {
        val T = this.mapper.getTableInfo(kclass = kclass)
        val cascadeSql = if (cascade) " CASCADE" else ""
        return Query(sql = "DROP TABLE IF EXISTS ${T.path}$cascadeSql")
    }

    /**
     * Creates a SQL string to create new table.
     *
     * @param kclass The Kotlin class representing the database table to which the row will be created.
     * @return The [Query] object representing the SQL query.
     */
    internal abstract fun <T : Any> createQuery(kclass: KClass<T>): Query

    /**
     * Creates a SQL string for deleting all rows from the specified table.
     *
     * @param kclass The Kotlin class representing the table to delete records from.
     * @return The [Query] object representing the delete query.
     */
    internal fun <T : Any> deleteQuery(kclass: KClass<T>): Query {
        val T = this.mapper.getTableInfo(kclass = kclass)
        return Query(sql = "DELETE FROM ${T.path}")
    }

    /**
     * Creates a SQL string to deletes specific row by id from the database table.
     *
     * @param obj The object representing the table from which row to be deleted.
     * @return The [Query] object representing the delete query.
     */
    internal fun deleteQuery(obj: Any): Query {
        val T = this.mapper.getTableInfo(obj = obj)
        return Query(
            sql = "DELETE FROM ${T.path} WHERE ${T.primaryKey.path} = ?",
            T.primaryKey.queryValue(obj)
        )
    }

    /**
     * Generates an SQL string for inserting new row.
     *
     * @param obj The object to be inserted into the database.
     * @param batch A flag indicating whether to use batch insertion. Default is false.
     * @return The [Query] object representing the SQL insert query.
     */
    internal open fun insertQuery(obj: Any, batch: Boolean): Query {
        val T = this.mapper.getTableInfo(obj = obj)
        return Query(
            sql = "INSERT INTO ${T.path} (${T.sqlInsertColumns()}) VALUES (${T.sqlInsertQuestions()})",
            *T.queryValues(obj = obj),
        )
    }

    /**
     * Generates an SQL string to update a row by specific id in the database table.
     *
     * @param obj The object representing the row to be updated.
     * @return The [Query] object representing the SQL update query.
     */
    internal fun updateQuery(obj: Any): Query {
        val T = this.mapper.getTableInfo(obj = obj)
        return Query(
            sql = "UPDATE ${T.path} SET ${T.sqlUpdateColumns()} WHERE ${T.primaryKey.path} = ?",
            *T.queryValues(obj = obj),
            T.primaryKey.queryValue(obj = obj)
        )
    }

    /**
     * Generates SQL string for selecting all rows from table.
     *
     * @param kclass The Kotlin class or table to query.
     * @return The Query object representing the SELECT query.
     */
    internal fun <T : Any> query(kclass: KClass<T>): Query {
        val T = this.mapper.getTableInfo(kclass = kclass)
        return Query(sql = "SELECT * FROM ${T.path}")
    }

    /**
     * Generates SQL string for selecting rows from table with pagination.
     *
     * @param kclass the Kotlin class to query from.
     * @param page the pagination details.
     * @return a Query object representing the executed database query.
     */
    internal open fun <T : Any> query(kclass: KClass<T>, page: Page<T>): Query {
        val T = this.mapper.getTableInfo(kclass = kclass)
        return Query(sql = "SELECT * FROM ${T.path} ORDER BY ${page.orderBy.name} ASC LIMIT ${page.limit} OFFSET ${page.offset}")
    }

    /**
     * Generates SQL string for a specific row based on the primary key value.
     *
     * @param kclass The class representing the table in the database.
     * @param pk The primary key value of the record to query.
     * @return A `Query` object representing the SQL query to fetch the record.
     */
    internal fun <T : Any, K : Any> query(kclass: KClass<T>, pk: K): Query {
        val T = this.mapper.getTableInfo(kclass = kclass)
        return Query(sql = "SELECT * FROM ${T.path} WHERE ${T.primaryKey.path}=$pk")
    }

    /**
     * Generates SQL string for calling stored procedure.
     *
     * @param obj The input object representing the stored procedure to be called.
     * @return A [Query] object representing the SQL query.
     * @throws SerializerException if the [Procedure] for the object cannot be found.
     */
    internal open fun <T : Any> callQuery(obj: T): Query {
        val P = this.mapper.getProcedure(obj = obj)
        return Query(sql = "CALL ${P.name}(${P.sqlArguments()}")
    }

    /**
     * Creates [Query] from user defined SQL string
     *
     * @param getSql A function that returns the user provided SQL statement to be executed.
     * @return A [Query] object representing the SQL query and its values.
     */
    internal fun query(getSql: () -> String): Query = Query(sql = getSql())

    /**
     * Creates [Query] from user defined SQL string with inputs.
     *
     * @param input The input object used to generate the query.
     * @param getSql A function that takes a QueryBuilder and returns the SQL string for the query.
     * @return A Query object representing the generated query.
     */
    internal fun <IN : Any> query(input: IN, getSql: (queryBuilder: QueryBuilder<IN>) -> String): Query {
        val qBuilder = QueryBuilder(mapper = this.mapper, input = input)
        return qBuilder.build(sql = getSql(qBuilder))
    }
}
