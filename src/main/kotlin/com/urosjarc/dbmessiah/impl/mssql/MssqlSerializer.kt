package com.urosjarc.dbmessiah.impl.mssql

import com.urosjarc.dbmessiah.Schema
import com.urosjarc.dbmessiah.Serializer
import com.urosjarc.dbmessiah.data.Query
import com.urosjarc.dbmessiah.data.TypeSerializer
import com.urosjarc.dbmessiah.domain.Page
import kotlin.reflect.KClass

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
    globalProcedures = globalProcedures
) {

    override val selectLastId = "SELECT SCOPE_IDENTITY()"

    override fun <T : Any> createTable(table: KClass<T>): Query {
        val T = this.mapper.getTableInfo(kclass = table)

        val col = mutableListOf<String>()
        val constraints = mutableListOf<String>()

        //Primary key
        val autoIncrement = if (T.primaryKey.autoIncrement) "IDENTITY(1,1)" else ""
        col.add("${T.primaryKey.name} ${T.primaryKey.dbType} PRIMARY KEY ${autoIncrement}")

        //Foreign keys
        T.foreignKeys.forEach {
            val isNull = if (it.notNull) "NOT NULL" else ""
            val isUnique = if (it.unique) "UNIQUE" else ""
            val isDeleteCascade = if (it.cascadeDelete) "ON DELETE CASCADE" else ""
            val isUpdateCascade = if (it.cascadeUpdate) "ON UPDATE CASCADE" else ""
            col.add("${it.name} ${it.dbType} $isNull $isUnique")
            constraints.add(
                "FOREIGN KEY (${it.name}) REFERENCES ${it.foreignTable.path} (${it.foreignTable.primaryKey.name}) $isUpdateCascade $isDeleteCascade"
            )
        }

        //Other columns
        T.otherColumns.forEach {
            val isNull = if (it.notNull) "" else "NOT NULL"
            val isUnique = if (it.unique) "UNIQUE" else ""
            col.add("${it.name} ${it.dbType} $isNull $isUnique")
        }

        //Connect all column definitions to one string
        val columns = (col + constraints).joinToString(", ")

        //Return created query
        return Query(sql = "CREATE TABLE ${T.path} ($columns)")
    }

    override fun <T : Any> selectTable(table: KClass<T>, page: Page<T>): Query {
        val T = this.mapper.getTableInfo(kclass = table)
        return Query(sql = "SELECT * FROM ${T.path} ORDER BY ${page.orderBy.name} OFFSET ${page.offset} ROWS FETCH NEXT ${page.limit} ROWS ONLY")
    }

    override fun <T : Any> dropTable(table: KClass<T>, cascade: Boolean): Query {
        val T = this.mapper.getTableInfo(kclass = table)
        return Query(sql = "DROP TABLE IF EXISTS ${T.path}")
    }

    public override fun <T : Any> callProcedure(obj: T): Query {
        val P = this.mapper.getProcedure(obj = obj)
        val args = P.args.map { "@${it.name} = ?" }.joinToString(", ")
        return Query(
            sql = "EXEC ${P.name} $args",
            *P.queryValues(obj = obj)
        )
    }

    /**
     * Generates SQL string for calling stored procedure.
     *
     * @param obj The input object representing the stored procedure to be called.
     * @return A [Query] object representing the SQL query.
     * @throws SerializerException if the [Procedure] for the object cannot be found.
     */
    override fun <T : Any> createProcedure(procedure: KClass<T>, body: () -> String): Query {
        val P = this.mapper.getProcedure(kclass = procedure)
        val args = P.args.map { "@${it.name} ${it.dbType}" }.joinToString(", ")
        return Query(
            sql = """
                CREATE OR ALTER PROCEDURE ${P.path} $args
                AS BEGIN
                    ${body()}
                END;
            """.trimIndent()
        )
    }

    /**
     * Creates a new database schema if it does not already exist.
     *
     * @param schema The [Schema] object representing the schema to be created.
     * @return A [Query] object representing the SQL query to create the schema.
     */
    override fun createSchema(schema: Schema): Query {
        return Query(
            sql = """
                IF NOT EXISTS (SELECT 1 FROM sys.schemas WHERE name = '${schema.name}')
                BEGIN
                    EXEC( 'CREATE SCHEMA ${schema.name}' );
                END
            """
        )
    }
}
