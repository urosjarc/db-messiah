package procedures

import com.urosjarc.dbmessiah.domain.Table
import com.urosjarc.dbmessiah.impl.mssql.MssqlSchema
import com.urosjarc.dbmessiah.impl.mssql.MssqlSerializer
import com.urosjarc.dbmessiah.impl.mssql.MssqlService
import com.urosjarc.dbmessiah.serializers.BasicTS
import java.util.*
import kotlin.test.assertEquals

/**
 * It's not best practice to have any logic inside database.
 * The ease of maintaining and understanding Kotlin code will be always superior to any code written inside database procedure.
 * It's ok to have small number of procedures inside you project but please of the love of Clean Architecture and Uncle Bob,
 * do write your core logic with proper programming language.
 */

/**
 * Define your domain classes (tables)...
 */
data class Parent(var pk: Int? = null, var value: String)

/**
 * Define Procedures with arguments as inputs...
 */
data class Procedure0(val value: Int)
data class Procedure1(val value: Int)


/**
 * Define your database schema with registered procedures...
 * In this demonstration I will be using MS SQL server since sqlite does not have procedures
 * and postgresql support only functions.
 */
val schema = MssqlSchema(
    name = "procedures",
    tables = listOf(Table(Parent::pk)),
    procedures = listOf(Procedure0::class, Procedure1::class) // Register procedures here...
)

val service = MssqlService(
    ser = MssqlSerializer(schemas = listOf(schema), globalSerializers = BasicTS.postgresql),
    config = Properties().apply {
        this["jdbcUrl"] = "jdbc:sqlserver://localhost:1433;encrypt=false;"
        this["username"] = "sa"
        this["password"] = "Root_root1"
    }
)

fun procedures() {
    service.autocommit {
        /**
         * Setup database
         */
        it.schema.create(schema = schema)
        it.table.drop<Parent>()
        it.table.create<Parent>()

        /**
         * Create some elements
         */
        val parent0 = Parent(value = "asdf")
        val parent1 = Parent(value = "xxxx")
        it.row.insert(row = parent0)
        it.row.insert(row = parent1)

        /**
         * To create the procedure you have to provide the class which will hold input values and
         * callback where you provide execution body for the procedure. Database serializer
         * will create proper SQL in background. Note that some databases have no support for the procedures.
         */
        it.procedure.create<Procedure0> {
            """SELECT * FROM procedures.Parent where pk = @${Procedure0::value.name};"""
        }

        /**
         * Call procedure by providing created object for that specific procedure
         * and output class that you want to receive back.
         */
        val parents0 = it.procedure.call(procedure = Procedure0(value = 1), Parent::class)
        assertEquals(parents0, listOf(parent0))

        /**
         * Create procedure with multiple output
         */
        it.procedure.create<Procedure1> {
            """
            SELECT * FROM procedures.Parent where pk = @${Procedure1::value.name};
            SELECT * FROM procedures.Parent;
            """
        }

        /**
         * Call procedure and get result.
         * Note that now that you want to receive multiple outputs the returning structure is matrix where each row represents
         * the results from that specific output.
         */
        val parents1: MutableList<List<Any>> = it.procedure.call(procedure = Procedure1(value = 1), Parent::class, Parent::class)
        assertEquals(parents1[0], listOf(parent0))
        assertEquals(parents1[1], listOf(parent0, parent1))
    }
}
