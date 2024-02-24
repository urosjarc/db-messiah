import com.urosjarc.dbmessiah.data.QueryBuilder
import com.urosjarc.dbmessiah.domain.Page
import com.urosjarc.dbmessiah.domain.Table
import com.urosjarc.dbmessiah.impl.sqlite.SqliteSerializer
import com.urosjarc.dbmessiah.impl.sqlite.SqliteService
import com.urosjarc.dbmessiah.serializers.AllTS
import java.awt.event.InputEvent
import kotlin.test.assertEquals

/**
 * 1. Reuse sqlite and postgresql service defined in 000_basic_sqlite and 001_basic_postgresql
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
 * 2. Create database serializer and explain database structure...
 */
val sqliteSerializer1 = SqliteSerializer(
    tables = listOf(
        Table(Parent2::pk),
        Table(
            Child2::pk, foreignKeys = listOf(
                Child2::parent_pk to Parent2::class
            )
        ),
    ),
    globalSerializers = AllTS.basic
)

val sqliteService1 = SqliteService(config = sqliteConfig, ser = sqliteSerializer1)

fun main_002() {
    sqliteService1.query {

        /**
         * 3. Create table for parent and child
         */
        it.table.create(table = Parent2::class)
        it.table.create(table = Child2::class)

        /**
         * 4. Write custom query without input or output
         *
         * If you are using JetBrains you can use SUPER DUPER "inject SQL language reference"
         * to the SQL string and execute it directly in the editor!!!
         *
         * > https://www.jetbrains.com/help/idea/using-language-injections.html
         */
        it.run.query { "INSERT INTO Parent2 (pk, value) VALUES (123, 'asdf')" }

        /**
         * 5. Write custom query with output
         */
        val output0 = it.run.query(output = Parent2::class) { "SELECT * FROM Parent2 WHERE pk = 123" }
        assertEquals(output0[0].pk, 123)

        /**
         * 6. Write SQL injection safe custom query with output and input
         *
         * For input objects you can use any table registered in serializer, if you use custom objects (not tables)
         * you will have to defined them to globalInputs in the serializer constructor to ensure type safety.
         */
        val output1 = it.run.query(
            output = Parent2::class,
            input = Child2(value = "asdf") // For our input we can also use the same table but lets use different so that it's not confusing...
        ) { q: QueryBuilder<Child2> ->     // If you are using input you will be provided with query builder to help you ensure type safety and prevent SQL injections...
            """SELECT * FROM Parent2 WHERE value = ${q.get(Child2::value)}
                
            -- q.get() will return '?' char back, so that JDBC can replace '?' with proper values to prevent SQL injection attacks...
            -- to read more about magic '?' character please refer to this link: https://docs.oracle.com/javase/tutorial/jdbc/basics/prepared.html
            -- q.get() will also register Kproperty1<INPUT, *> and find this property in input object to supply JDBC appropriate value to be replaced instead of '?' character.
            """
        }
        assertEquals(output1[0].pk, 123)

    }
}
