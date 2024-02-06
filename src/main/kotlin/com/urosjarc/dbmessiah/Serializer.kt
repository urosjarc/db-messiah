package com.urosjarc.dbmessiah

import com.urosjarc.dbmessiah.domain.querie.Query
import com.urosjarc.dbmessiah.domain.querie.QueryBuilder
import com.urosjarc.dbmessiah.domain.serialization.TypeSerializer
import com.urosjarc.dbmessiah.domain.table.Page
import com.urosjarc.dbmessiah.exceptions.MapperException
import kotlin.reflect.KClass

abstract class Serializer(
    val schemas: List<Schema>,
    val globalSerializers: List<TypeSerializer<*>>,
    val globalInputs: List<KClass<*>>,
    val globalOutputs: List<KClass<*>>,
    val globalProcedures: List<KClass<*>> = listOf()
) {

    val mapper = Mapper(
        schemas = schemas.toList(),
        globalSerializers = globalSerializers,
        globalInputs = globalInputs,
        globalOutputs = globalOutputs,
        globalProcedures = globalProcedures
    )

    /**
     * MANAGING TABLES
     */
    open val selectLastId: String? = null

    fun plantUML(): String {
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

    fun createQuery(schema: Schema) = Query(sql = "CREATE SCHEMA IF NOT EXISTS ${schema.name}")
    fun dropQuery(schema: Schema) = Query(sql = "DROP SCHEMA IF EXISTS ${schema.name}")

    open fun <T : Any> dropQuery(kclass: KClass<T>, cascade: Boolean = false): Query {
        val T = this.mapper.getTableInfo(kclass = kclass)
        val cascadeSql = if (cascade) " CASCADE" else ""
        return Query(sql = "DROP TABLE IF EXISTS ${T.path}$cascadeSql")
    }

    abstract fun <T : Any> createQuery(kclass: KClass<T>): Query
    fun <T : Any> deleteQuery(kclass: KClass<T>): Query {
        val T = this.mapper.getTableInfo(kclass = kclass)
        return Query(sql = "DELETE FROM ${T.path}")
    }

    /**
     * MANAGING ROWS
     */
    fun deleteQuery(obj: Any): Query {
        val T = this.mapper.getTableInfo(obj = obj)
        return Query(
            sql = "DELETE FROM ${T.path} WHERE ${T.primaryKey.path} = ?",
            T.primaryKey.queryValue(obj)
        )
    }

    open fun insertQuery(obj: Any, batch: Boolean): Query {
        val T = this.mapper.getTableInfo(obj = obj)
        return Query(
            sql = "INSERT INTO ${T.path} (${T.sqlInsertColumns()}) VALUES (${T.sqlInsertQuestions()})",
            *T.queryValues(obj = obj),
        )
    }

    fun updateQuery(obj: Any): Query {
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
    fun <T : Any> query(kclass: KClass<T>): Query {
        val T = this.mapper.getTableInfo(kclass = kclass)
        return Query(sql = "SELECT * FROM ${T.path}")
    }

    open fun <T : Any> query(kclass: KClass<T>, page: Page<T>): Query {
        val T = this.mapper.getTableInfo(kclass = kclass)
        return Query(sql = "SELECT * FROM ${T.path} ORDER BY ${page.orderBy.name} ASC LIMIT ${page.limit} OFFSET ${page.offset}")
    }

    fun <T : Any, K : Any> query(kclass: KClass<T>, pk: K): Query {
        val T = this.mapper.getTableInfo(kclass = kclass)
        return Query(sql = "SELECT * FROM ${T.path} WHERE ${T.primaryKey.path}=$pk")
    }

    /**
     * Call procedure
     */
    open fun <T : Any> callQuery(obj: T): Query {
        val P = this.mapper.getProcedure(obj = obj)
        return Query(sql = "CALL ${P.name}(${P.sqlArguments()}")
    }

    /**
     * Generic queries
     */
    fun query(getSql: () -> String): Query = Query(sql = getSql())
    fun <IN : Any> query(input: IN, getSql: (queryBuilder: QueryBuilder<IN>) -> String): Query {
        val qBuilder = QueryBuilder(mapper = this.mapper, input = input)
        return qBuilder.build(sql = getSql(qBuilder))
    }

}
