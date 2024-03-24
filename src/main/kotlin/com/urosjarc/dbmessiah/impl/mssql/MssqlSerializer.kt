package com.urosjarc.dbmessiah.impl.mssql

import com.urosjarc.dbmessiah.Schema
import com.urosjarc.dbmessiah.Serializer
import com.urosjarc.dbmessiah.data.Query
import com.urosjarc.dbmessiah.data.TypeSerializer
import com.urosjarc.dbmessiah.domain.Page
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

public open class MssqlSerializer(
    schemas: List<MssqlSchema> = listOf(),
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
     * SQL server does not return generated UUID from database.
     */
    allowAutoUUID = false
) {

    override fun escaped(name: String): String = "[$name]"
    override fun <T : Any> escaped(procedureArg: KProperty1<T, *>): String = "@${procedureArg.name}"
    override fun <T : Any> createTable(table: KClass<T>): Query {
        val T = this.mapper.getTableInfo(kclass = table)

        val col = mutableListOf<String>()
        val constraints = mutableListOf<String>()

        //Primary key
        val default =
            if (T.primaryColumn.autoInc) " IDENTITY(1,1)"
            else if (T.primaryColumn.autoUUID) " DEFAULT NEWID()"
            else ""

        col.add("${escaped(T.primaryColumn.name)} ${T.primaryColumn.dbType} PRIMARY KEY${default}")

        //Foreign keys
        T.foreignColumns.forEach {
            val notNull = if (it.notNull) " NOT NULL" else ""
            val unique = if (it.unique) " UNIQUE" else ""
            val deleteCascade = if (it.cascadeDelete) " ON DELETE CASCADE" else ""
            val updateCascade = if (it.cascadeUpdate) " ON UPDATE CASCADE" else ""
            col.add("${escaped(it.name)} ${it.dbType}$notNull$unique")
            constraints.add(
                "FOREIGN KEY (${escaped(it.name)}) REFERENCES ${escaped(it.foreignTable)} (${escaped(it.foreignTable.primaryColumn.name)})$updateCascade$deleteCascade"
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

    override fun <T : Any> selectTable(table: KClass<T>, page: Page<T>): Query {
        val T = this.mapper.getTableInfo(kclass = table)
        return Query(sql = "SELECT * FROM ${escaped(T)} ORDER BY ${escaped(page.orderBy.name)} OFFSET ${page.offset} ROWS FETCH NEXT ${page.limit} ROWS ONLY")
    }

    override fun <T : Any> dropTable(table: KClass<T>, cascade: Boolean): Query {
        val T = this.mapper.getTableInfo(kclass = table)
        return Query(sql = "DROP TABLE IF EXISTS ${escaped(T)}")
    }

    public override fun <T : Any> callProcedure(procedure: T): Query {
        val P = this.mapper.getProcedure(obj = procedure)
        // Here we don't escape arguments since @ signs is responsible for escaping argument
        val args = P.args.map { "@${it.name} = ?" }.joinToString(", ")
        return Query(
            sql = "EXEC ${escaped(P)} $args",
            *P.queryValues(obj = procedure)
        )
    }

    override fun <T : Any> dropProcedure(procedure: KClass<T>): Query {
        val P = this.mapper.getProcedure(kclass = procedure)
        return Query(sql = "DROP PROCEDURE IF EXISTS ${escaped(P)}")
    }

    public override fun <T : Any> createProcedure(procedure: KClass<T>, procedureBody: String): Query {
        val P = this.mapper.getProcedure(kclass = procedure)
        // Here we don't escape arguments since @ signs is responsible for escaping argument
        val args = P.args.map { "@${it.name} ${it.dbType}" }.joinToString(", ")
        return Query(
            sql = """
                CREATE OR ALTER PROCEDURE ${escaped(P)} $args
                AS BEGIN
                    $procedureBody
                END;
            """.trimIndent()
        )
    }

    override fun createSchema(schema: Schema): Query {
        return Query(
            sql = """
                IF NOT EXISTS (SELECT 1 FROM sys.schemas WHERE name = '${schema.name}')
                BEGIN
                    EXEC( 'CREATE SCHEMA ${escaped(schema.name)}' );
                END;
            """.trimIndent()
        )
    }
}
