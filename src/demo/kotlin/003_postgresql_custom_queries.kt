import com.urosjarc.dbmessiah.domain.Table
import com.urosjarc.dbmessiah.impl.postgresql.PgSchema
import com.urosjarc.dbmessiah.impl.postgresql.PgSerializer
import com.urosjarc.dbmessiah.impl.postgresql.PgService
import com.urosjarc.dbmessiah.serializers.AllTS
import kotlin.test.assertEquals

/**
 * 1. Reuse sqlite and postgresql service defined in 000_basic_sqlite and 001_basic_postgresql
 */
data class Parent3(
    var pk: Int? = null,
    var value: String
)

data class Child3(
    var pk: Int? = null,
    val parent_pk: Int? = null,
    var value: String
)

data class Output3(
    val child_value: String,
    val parent_value: String
)

data class Input3(
    val parent_pk: Int
)

val mainSchema = PgSchema(
    name = "main", tables = listOf(
        Table(Parent3::pk),
        Table(
            Child3::pk, foreignKeys = listOf(
                Child3::parent_pk to Parent3::class
            )
        )
    )
)

/**
 * 2. Create database serializer and explain database structure...
 */
val postgresqlSerializer1 = PgSerializer(
    schemas = listOf(mainSchema),
    globalSerializers = AllTS.basic,
    globalOutputs = listOf(Output3::class), // Note if you use custom objects as input or output you have to register them to global inputs or outputs.
    globalInputs = listOf(Input3::class),   // This is because library uses reflection at initilization to scan objects of their properties, constructors etc...
)

val postgresqlService1 = PgService(config = postgresqlConfig, ser = postgresqlSerializer1)

fun main_003() {
    postgresqlService1.query { it ->
        it.table.dropCascade(table = Parent3::class)
        it.table.dropCascade(table = Child3::class)

        /**
         * 3. Create table for parent and child
         */
        it.table.create(table = Parent3::class)
        it.table.create(table = Child3::class)

        /**
         * 4. Write custom query without input or output
         *
         * Sqlite driver support only one query per database call,
         * Postgresql driver on other hand it supports many per db call...
         */
        it.run.query {
            """
            INSERT INTO main.Parent3 (pk, value) VALUES (1, 'parent_1');
            INSERT INTO main.Parent3 (pk, value) VALUES (2, 'parent_2');
            INSERT INTO main.Parent3 (pk, value) VALUES (3, 'parent_3');
            INSERT INTO main.Parent3 (pk, value) VALUES (4, 'parent_4');
            """
        }

        it.run.query {
            """
            INSERT INTO main.Child3 (pk, parent_pk, value) VALUES (1, 1, 'child_1');
            INSERT INTO main.Child3 (pk, parent_pk, value) VALUES (2, 1, 'child_2');
            INSERT INTO main.Child3 (pk, parent_pk, value) VALUES (3, 2, 'child_3');
            INSERT INTO main.Child3 (pk, parent_pk, value) VALUES (4, 2, 'child_4');
            """
        }

        /**
         * 5. Write custom query with multiple output
         *
         * Note that now returning structure will be 2D list of Any objects where each row represents results
         * from one query. If you would like to peak inside list you will have to cast it to specific type...
         */
        val matrix0 = it.run.query(Parent3::class, Child3::class) {
            """
            SELECT * FROM main.Parent3;  
            SELECT * FROM main.Child3 WHERE pk > 2  
            """
        }
        assertEquals(matrix0[0].size, 4) // Check the matrix size.
        assertEquals(matrix0[1].size, 2) // Check the matrix size.
        assertEquals((matrix0[0] as List<Parent3>).size, 4) // Check the specific row with casting.

        /**
         * 6. Write custom query with multiple output and input
         */
        val matrix1 = it.run.query(Parent3::class, Child3::class, input = Input3(parent_pk = 3)) {
            """
            SELECT * FROM main.Parent3 WHERE pk = ${it.put(Input3::parent_pk)};
            SELECT * FROM main.Child3 WHERE pk = 2  
            """
        }
        val parent3 = matrix1[0][0] as Parent3
        val child3 = matrix1[1][0] as Child3
        assertEquals(parent3.pk, 3)
        assertEquals(child3.pk, 2)
    }
}
