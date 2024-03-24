package query_sqlite

import com.urosjarc.dbmessiah.builders.QueryBuilder
import com.urosjarc.dbmessiah.domain.Table
import com.urosjarc.dbmessiah.impl.sqlite.SqliteSerializer
import com.urosjarc.dbmessiah.impl.sqlite.SqliteService
import com.urosjarc.dbmessiah.serializers.BasicTS
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Define your domain classes (tables)...
 */
data class Parent(var pk: Int? = null, var value: String)
data class Child(var pk: Int? = null, val parent_pk: Int? = null, var value: String)

/**
 * Output and input classes that will be used in the custom queries.
 * Input class will provide input values...
 * Output classes will hold output results...
 */
data class Output(val child_value: String, val parent_value: String)
data class Input(val parent_pk: Int)

/**
 * Create database serializer and explain database structure...
 */
val serializer = SqliteSerializer(
    tables = listOf(
        Table(Parent::pk),
        Table(Child::pk, foreignKeys = listOf(Child::parent_pk to Parent::class)),
    ),
    globalSerializers = BasicTS.sqlite,
    globalOutputs = listOf(Output::class), // Note if you use custom objects as input or output you have to register them to global inputs or outputs.
    globalInputs = listOf(Input::class),   // This is because library uses reflection at initialization to scan objects of their properties, constructors etc...
)

val service = SqliteService(ser = serializer, config = Properties().apply { this["jdbcUrl"] = "jdbc:sqlite::memory:" })

fun query_sqlite() {
    service.autocommit {

        /**
         * Create table for parent and child.
         */
        it.table.create<Parent>()
        it.table.create<Child>()

        /**
         * Write UNSAFE custom query without input or output.
         * If you are using JetBrains you can use SUPER DUPER "inject SQL language reference"
         * to the SQL string and execute it directly in the editor to check SQL validity!!!
         * > https://www.jetbrains.com/help/idea/using-language-injections.html
         */
        it.query.run { "INSERT INTO Parent (pk, value) VALUES (1, 'parent_asdf')" }
        it.query.run { "INSERT INTO Child (pk, parent_pk, value) VALUES (1, 1, 'child_asdf')" }

        /**
         * Write UNSAFE custom query with output.
         */
        val output0 = it.query.get<Parent> { "SELECT * FROM Parent WHERE pk = 1" }
        val output1 = it.query.get<Child> { "SELECT * FROM Child" }
        assertEquals(output0[0].pk, 1)
        assertEquals(output1.size, 1)

        /**
         * Write SAFE custom queries with output.
         * For input objects you can use any table registered in serializer, if you use custom objects (not tables)
         * you will have to defined them to globalInputs in the serializer constructor to ensure type safety.
         * Method input() will return '?' character back, so that JDBC can replace '?' with proper values to prevent SQL injection attacks...
         * To read more about magical '?' character please refer to this link: https://docs.oracle.com/javase/tutorial/jdbc/basics/prepared.html
         * Method input() will also register Kproperty1<INPUT, *> and find this property in input object to supply JDBC appropriate value to be replaced instead of '?' character.
         */
        val output2 = it.query.get(
            output = Parent::class,              // We want to retrieve rows from parent table
            input = Child(value = "parent_asdf") // For our input we can also use the same table but lets use different so that it's not confusing...
        ) { q: QueryBuilder<Child> ->            // If you are using input you will be provided with query builder context to help you ensure type safety and prevent SQL injections...
            """
            ${q.SELECT<Parent>()} WHERE ${q.column(Parent::value)} = ${q.input(Child::value)}
            """
        }
        assertEquals(output2[0].pk, 1)

        /**
         * Let's increase difficulty with some SAFE JOIN statements.
         * Those are super intuitive. If you know raw SQL then you are good to go.
         */
        val output3 = it.query.get(output = Child::class, input = Input(parent_pk = 1)) {
            """
            ${it.SELECT<Child>()}
                JOIN ${it.table<Parent>()} ON ${it.column(Parent::pk)} = ${it.column(Child::parent_pk)}
            WHERE
                ${it.column(Parent::pk)} = ${it.input(Input::parent_pk)}
            """
        }
        assertTrue(output3.isNotEmpty())

        /**
         * Write custom query with custom output and input in case you want to retrieve only specific columns.
         * Order of listed output columns doesn't need to be in order but the names must match with output columns names, use 'AS' for that.
         * Note that Sqlite driver only supports one output per database call that's why you can have only one output!
         * Other databases can support many outputs per output since underling driver supports it...
         * > https://github.com/xerial/sqlite-jdbc/issues/1062
         */
        val output4 = it.query.get(output = Output::class, input = Input(parent_pk = 1)) {
            """
            SELECT
                ${it.column(Child::value)} AS ${it.name(Output::child_value)}, 
                ${it.column(Parent::value)} AS ${it.name(Output::parent_value)} 
            FROM ${it.table<Child>()}
                JOIN ${it.table<Parent>()} ON ${it.column(Parent::pk)} = ${it.column(Child::parent_pk)}
            WHERE ${it.column(Parent::pk)} = ${it.input(Input::parent_pk)}
            """
        }

        assertTrue(output4.isNotEmpty())

        /**
         * Here is represented the final SQL statement that will be executed on database.
         * Notice the question mark which prevents SQL injection attacks and escaped quotations
         * around schema, table, column which allows the use of SQL reserved words for naming elements.
         *
         *    SELECT
         *        "main"."Child"."value" AS "child_value",
         *        "main"."Parent"."value" AS "parent_value"
         *    FROM "main"."Child"
         *        JOIN "main"."Parent" ON "main"."Parent"."pk" = "main"."Child"."parent_pk"
         *    WHERE "main"."Parent"."pk" = ?
         */

    }
}
