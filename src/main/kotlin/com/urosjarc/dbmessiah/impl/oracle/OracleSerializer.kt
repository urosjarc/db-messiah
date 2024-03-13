package com.urosjarc.dbmessiah.impl.oracle

import com.urosjarc.dbmessiah.Serializer
import com.urosjarc.dbmessiah.data.Query
import com.urosjarc.dbmessiah.data.TypeSerializer
import com.urosjarc.dbmessiah.domain.Page
import kotlin.reflect.KClass

public open class OracleSerializer(
    schemas: List<OracleSchema> = listOf(),
    globalSerializers: List<TypeSerializer<*>> = listOf(),
    globalInputs: List<KClass<*>> = listOf(),
    globalOutputs: List<KClass<*>> = listOf(),
    globalProcedures: List<KClass<*>> = listOf()
) : Serializer(
    schemas = schemas,
    globalSerializers = globalSerializers,
    globalInputs = globalInputs,
    globalOutputs = globalOutputs,
    globalProcedures = globalProcedures,
    /**
     * Generation of UUID is to complex (it can be done but not in this version).
     */
    allowAutoUUID = false
) {
    override val selectLastId: String? = null
    override fun escaped(name: String): String = "\"$name\""

    override fun <T : Any> createTable(table: KClass<T>): Query {
        val T = this.mapper.getTableInfo(kclass = table)

        val col = mutableListOf<String>()
        val constraints = mutableListOf<String>()

        //PRIMARY KEY
        val default =
            if (T.primaryColumn.autoInc) " GENERATED BY DEFAULT ON NULL AS IDENTITY"
            else ""

        col.add("${escaped(T.primaryColumn.name)} ${T.primaryColumn.dbType}${default}")
        constraints.add("PRIMARY KEY (${escaped(T.primaryColumn.name)})")

        //Foreign keys
        T.foreignColumns.forEach {
            val notNull = if (it.notNull) " NOT NULL" else ""
            val unique = if (it.unique) " UNIQUE" else ""
            val deleteCascade = if (it.cascadeDelete) " ON DELETE CASCADE" else ""
            val updateCascade = if (it.cascadeUpdate) " ON UPDATE CASCADE" else ""
            col.add("${escaped(it.name)} ${it.dbType}$notNull$unique")
            constraints.add(
                "FOREIGN KEY (${escaped(it.name)}) REFERENCES ${escaped(it.foreignTable)}(${escaped(it.foreignTable.primaryColumn.name)})$updateCascade$deleteCascade"
            )
        }

        //Other columns
        T.otherColumns.forEach {
            val notNull = if (it.notNull) " NOT NULL" else ""
            val unique = if (it.unique) " UNIQUE" else ""
            col.add("${escaped(it.name)} ${it.dbType}$notNull$unique")
        }

        //Connect all column definitions to one string
        val columns = (col + constraints).joinToString(", ")

        //Return created query
        return Query(sql = "CREATE TABLE ${escaped(T)} ($columns)")
    }

    public override fun <T : Any> selectTable(table: KClass<T>, page: Page<T>): Query {
        val T = this.mapper.getTableInfo(kclass = table)
        return Query(sql = "SELECT * FROM ${escaped(T)} ORDER BY ${escaped(page.orderBy.name)} OFFSET ${page.offset} ROWS FETCH NEXT ${page.limit} ROWS ONLY")
    }

    override fun <T : Any> dropTable(table: KClass<T>, cascade: Boolean): Query {
        val T = this.mapper.getTableInfo(kclass = table)
        val cascadeSql = if (cascade) " CASCADE CONSTRAINTS" else ""
        return Query(sql = "DROP TABLE ${escaped(T)}$cascadeSql")
    }


    public override fun <T : Any> callProcedure(procedure: T): Query {
        val P = this.mapper.getProcedure(obj = procedure)
        var args = P.args.map { "${escaped(it.name)} => ?" }.joinToString(", ")
        args = if (P.args.isEmpty()) "()" else "($args)"
        return Query(
            sql = "CALL ${escaped(P)}$args",
            *P.queryValues(obj = procedure)
        )
    }

    override fun <T : Any> dropProcedure(procedure: KClass<T>): Query {
        val P = this.mapper.getProcedure(kclass = procedure)
        return Query(sql = "DROP PROCEDURE ${escaped(P)}")
    }

    public override fun <T : Any> createProcedure(procedure: KClass<T>, procedureBody: String): Query {
        val P = this.mapper.getProcedure(kclass = procedure)
        var args = P.args.map { "${escaped(it.name)} ${it.dbType.split("(").first()}" }.joinToString(", ")
        args = if (P.args.isEmpty()) "" else "($args)"
        return Query(
            sql = """
                CREATE OR REPLACE PROCEDURE ${escaped(P)}$args
                AS BEGIN
                    $procedureBody
                END;
            """.trimIndent()
        )
    }

}
