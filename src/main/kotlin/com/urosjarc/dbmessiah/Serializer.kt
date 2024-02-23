package com.urosjarc.dbmessiah

import com.urosjarc.dbmessiah.domain.Page
import com.urosjarc.dbmessiah.data.Query
import com.urosjarc.dbmessiah.data.QueryBuilder
import com.urosjarc.dbmessiah.data.TypeSerializer
import com.urosjarc.dbmessiah.exceptions.MapperException
import kotlin.reflect.KClass

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
     * MANAGING TABLES
     */
    internal open val selectLastId: String? = null

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

    internal fun createQuery(schema: Schema) = Query(sql = "CREATE SCHEMA IF NOT EXISTS ${schema.name}")
    internal fun dropQuery(schema: Schema) = Query(sql = "DROP SCHEMA IF EXISTS ${schema.name}")

    internal open fun <T : Any> dropQuery(kclass: KClass<T>, cascade: Boolean = false): Query {
        val T = this.mapper.getTableInfo(kclass = kclass)
        val cascadeSql = if (cascade) " CASCADE" else ""
        return Query(sql = "DROP TABLE IF EXISTS ${T.path}$cascadeSql")
    }

    internal abstract fun <T : Any> createQuery(kclass: KClass<T>): Query
    internal fun <T : Any> deleteQuery(kclass: KClass<T>): Query {
        val T = this.mapper.getTableInfo(kclass = kclass)
        return Query(sql = "DELETE FROM ${T.path}")
    }

    /**
     * MANAGING ROWS
     */
    internal fun deleteQuery(obj: Any): Query {
        val T = this.mapper.getTableInfo(obj = obj)
        return Query(
            sql = "DELETE FROM ${T.path} WHERE ${T.primaryKey.path} = ?",
            T.primaryKey.queryValue(obj)
        )
    }

    internal open fun insertQuery(obj: Any, batch: Boolean): Query {
        val T = this.mapper.getTableInfo(obj = obj)
        return Query(
            sql = "INSERT INTO ${T.path} (${T.sqlInsertColumns()}) VALUES (${T.sqlInsertQuestions()})",
            *T.queryValues(obj = obj),
        )
    }

    internal fun updateQuery(obj: Any): Query {
        val T = this.mapper.getTableInfo(obj = obj)
        return Query(
            sql = "UPDATE ${T.path} SET ${T.sqlUpdateColumns()} WHERE ${T.primaryKey.path} = ?",
            *T.queryValues(obj = obj),
            T.primaryKey.queryValue(obj = obj)
        )
    }

    /**
     * SELECTS
     */
    internal fun <T : Any> query(kclass: KClass<T>): Query {
        val T = this.mapper.getTableInfo(kclass = kclass)
        return Query(sql = "SELECT * FROM ${T.path}")
    }

    internal open fun <T : Any> query(kclass: KClass<T>, page: Page<T>): Query {
        val T = this.mapper.getTableInfo(kclass = kclass)
        return Query(sql = "SELECT * FROM ${T.path} ORDER BY ${page.orderBy.name} ASC LIMIT ${page.limit} OFFSET ${page.offset}")
    }

    internal fun <T : Any, K : Any> query(kclass: KClass<T>, pk: K): Query {
        val T = this.mapper.getTableInfo(kclass = kclass)
        return Query(sql = "SELECT * FROM ${T.path} WHERE ${T.primaryKey.path}=$pk")
    }

    /**
     * Call procedure
     */
    internal open fun <T : Any> callQuery(obj: T): Query {
        val P = this.mapper.getProcedure(obj = obj)
        return Query(sql = "CALL ${P.name}(${P.sqlArguments()}")
    }

    /**
     * Generic queries
     */
    internal fun query(getSql: () -> String): Query = Query(sql = getSql())
    internal fun <IN : Any> query(input: IN, getSql: (queryBuilder: QueryBuilder<IN>) -> String): Query {
        val qBuilder = QueryBuilder(mapper = this.mapper, input = input)
        return qBuilder.build(sql = getSql(qBuilder))
    }
}
