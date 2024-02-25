import com.urosjarc.dbmessiah.data.QueryBuilder
import com.urosjarc.dbmessiah.domain.Table
import com.urosjarc.dbmessiah.impl.sqlite.SqliteSerializer
import com.urosjarc.dbmessiah.impl.sqlite.SqliteService
import com.urosjarc.dbmessiah.serializers.AllTS
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Define your domain classes (tables)...
 */
data class Parent2(
    var pk: Int? = null,
    var value: String
)

data class Child2(
    var pk: Int? = null,
    val parent_pk: Int? = null,
    var value: String
)

/**
 * Output and input classes that will be used in the custom queries.
 * Input classes will provide values to custom queries...
 * Output classes will hold output results from the queries...
 */
data class Output2(
    val child_value: String,
    val parent_value: String
)

data class Input2(
    val parent_pk: Int
)

/**
 * Create database serializer and explain database structure...
 */
val ser2 = SqliteSerializer(
    tables = listOf(
        Table(Parent2::pk),
        Table(
            Child2::pk, foreignKeys = listOf(
                Child2::parent_pk to Parent2::class
            )
        ),
    ),
    globalSerializers = AllTS.basic,
    globalOutputs = listOf(Output2::class), // Note if you use custom objects as input or output you have to register them to global inputs or outputs.
    globalInputs = listOf(Input2::class),   // This is because library uses reflection at initialization to scan objects of their properties, constructors etc...
)

val service2 = SqliteService(config = config0, ser = ser2)

fun main_002() {
    service2.autocommit {

        /**
         * Create table for parent and child.
         */
        it.table.create(table = Parent2::class)
        it.table.create(table = Child2::class)

        /**
         * Write custom query without input or output.
         * If you are using JetBrains you can use SUPER DUPER "inject SQL language reference"
         * to the SQL string and execute it directly in the editor to check SQL validity!!!
         * > https://www.jetbrains.com/help/idea/using-language-injections.html
         */
        it.run.query { "INSERT INTO Parent2 (pk, value) VALUES (1, 'parent_asdf')" }
        it.run.query { "INSERT INTO Child2 (pk, parent_pk, value) VALUES (1, 1, 'child_asdf')" }

        /**
         * Write custom query with output.
         */
        val output0 = it.run.query(output = Parent2::class) { "SELECT * FROM Parent2 WHERE pk = 1" }
        val output1 = it.run.query(output = Child2::class) { "SELECT * FROM Child2" }
        assertEquals(output0[0].pk, 1)
        assertEquals(output1.size, 1)

        /**
         * Write SQL injection safe custom query with output and input.
         * For input objects you can use any table registered in serializer, if you use custom objects (not tables)
         * you will have to defined them to globalInputs in the serializer constructor to ensure type safety.
         */
        val output2 = it.run.query(
            output = Parent2::class,
            input = Child2(value = "parent_asdf") // For our input we can also use the same table but lets use different so that it's not confusing...
        ) { q: QueryBuilder<Child2> ->            // If you are using input you will be provided with query builder to help you ensure type safety and prevent SQL injections...
            """SELECT * FROM Parent2 WHERE value = ${q.put(Child2::value)}
                
            -- q.put() will return '?' character back, so that JDBC can replace '?' with proper values to prevent SQL injection attacks...
            -- to read more about magic '?' character please refer to this link: https://docs.oracle.com/javase/tutorial/jdbc/basics/prepared.html
            -- q.put() will also register Kproperty1<INPUT, *> and find this property in input object to supply JDBC appropriate value to be replaced instead of '?' character.
            """
        }
        assertEquals(output2[0].pk, 1)

        /**
         * Write custom query with custom output and input.
         * Note that Sqlite driver only supports one output per database call thats why you can have only one output!
         * Other databases can support many outputs per output since underling driver supports it...
         * > https://github.com/xerial/sqlite-jdbc/issues/1062
         * Those who use JetBrains please don't forget to inject sql string with reference so that you can directly test sql string on your database.
         * > https://youtrack.jetbrains.com/issue/DBE-20046/Support-for-in-code-SQL-execution-on-string-templates-with
         */
        val output3 = it.run.query(
            output = Output2::class,
            input = Input2(parent_pk = 1)
        ) {
            """
            SELECT
                c.value AS ${Output2::child_value.name}, 
                p.value AS ${Output2::parent_value.name} 
            FROM Child2 c
                JOIN Parent2 p ON c.parent_pk = p.pk
            WHERE p.pk = ${it.put(Input2::parent_pk)}
            """
        }

        assertTrue(output3.isNotEmpty())

    }
}
