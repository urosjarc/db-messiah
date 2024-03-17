package query_postgresql

import com.urosjarc.dbmessiah.domain.Table
import com.urosjarc.dbmessiah.impl.postgresql.PgSchema
import com.urosjarc.dbmessiah.impl.postgresql.PgSerializer
import com.urosjarc.dbmessiah.impl.postgresql.PgService
import com.urosjarc.dbmessiah.serializers.AllTS
import java.util.*
import kotlin.test.assertEquals

/**
 * Define your domain classes (tables, schemas)...
 */
data class Parent(var pk: Int? = null, var value: String)
data class Child(var pk: Int? = null, val parent_pk: Int? = null, var value: String)
data class Output(val child_value: String, val parent_value: String)
data class Input(val parent_pk: Int)

val schema = PgSchema(
    name = "query_postgresql", tables = listOf(
        Table(Parent::pk),
        Table(Child::pk, foreignKeys = listOf(Child::parent_pk to Parent::class))
    )
)

/**
 * Create database serializer and explain database structure...
 */
val service = PgService(ser = PgSerializer(
    schemas = listOf(schema),
    globalSerializers = AllTS.postgresql,
    globalOutputs = listOf(Output::class), // Note if you use custom objects as input or output you have to register them to global inputs or outputs.
    globalInputs = listOf(Input::class),   // This is because library uses reflection at initilization to scan objects of their properties, constructors etc...
), config = Properties().apply {
    this["jdbcUrl"] = "jdbc:postgresql://localhost:5432/public"
    this["username"] = "root"
    this["password"] = "root"
})

fun query_postgresql() {
    service.autocommit { it ->
        /**
         * Setup schema
         */
        it.schema.create(schema = schema)
        it.table.dropCascade<Parent>()
        it.table.dropCascade<Child>()

        /**
         * Create table for parent and child
         */
        it.table.create<Parent>()
        it.table.create<Child>()

        /**
         * Write UNSAFE custom query without input or output
         * Sqlite driver support only one query per database call,
         * but postgresql driver on the other hand supports many queries per db call.
         * This will increase speed and efficiency of your application.
         */
        it.query.run {
            """
            INSERT INTO ${it.table<Parent>()} ("pk", "value") VALUES (1, 'parent_1');
            INSERT INTO ${it.table<Parent>()} ("pk", "value") VALUES (2, 'parent_2');
            INSERT INTO ${it.table<Parent>()} ("pk", "value") VALUES (3, 'parent_3');
            INSERT INTO ${it.table<Parent>()} ("pk", "value") VALUES (4, 'parent_4');
            """
        }

        it.query.run {
            """
            INSERT INTO ${it.table<Child>()} ("pk", "parent_pk", "value") VALUES (1, 1, 'child_1');
            INSERT INTO ${it.table<Child>()} ("pk", "parent_pk", "value") VALUES (2, 1, 'child_2');
            INSERT INTO ${it.table<Child>()} ("pk", "parent_pk", "value") VALUES (3, 2, 'child_3');
            INSERT INTO ${it.table<Child>()} ("pk", "parent_pk", "value") VALUES (4, 2, 'child_4');
            """
        }

        /**
         * Write SAFE custom query with one or multiple output.
         * Note that if you use multiple outputs the returning structure will be 2D list of Any objects where each row represents results from one query.
         * If you would like to peak inside list you will have to cast it to specific type.
         * If you use singe output, function will not return matrix but a list on which you don't have to be cast since there is only one output type provided.
         */
        val matrix0 = it.query.get(Parent::class, Child::class) {
            """
            ${it.SELECT<Parent>()};
            ${it.SELECT<Child>()} WHERE ${it.column(Child::pk)} > 2  
            """
        }
        assertEquals(matrix0[0].size, 4) // Check the matrix size.
        assertEquals(matrix0[1].size, 2) // Check the matrix size.
        assertEquals((matrix0[0] as List<Parent>).size, 4) // Check the specific row with casting.

        /**
         * Write SAFE custom query with multiple output and input.
         * Joust like in the previous example the output structure depends on number of outputs,
         * if you will use multiple outputs the resulting output will be matrix and if you use single output
         * you will get list.
         */
        val matrix1 = it.query.get(Parent::class, Child::class, input = Input(parent_pk = 3)) {
            """
            ${it.SELECT<Parent>()} WHERE ${it.column(Parent::pk)} = ${it.input(Input::parent_pk)};
            ${it.SELECT<Child>()} WHERE ${it.column(Child::pk)} = 2  
            """
        }
        val parent0 = matrix1[0][0] as Parent
        val child0 = matrix1[1][0] as Child
        assertEquals(parent0.pk, 3)
        assertEquals(child0.pk, 2)
    }
}
