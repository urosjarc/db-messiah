import com.urosjarc.dbmessiah.data.TypeSerializer
import com.urosjarc.dbmessiah.domain.Table
import com.urosjarc.dbmessiah.impl.postgresql.PgSchema
import com.urosjarc.dbmessiah.impl.postgresql.PgSerializer
import com.urosjarc.dbmessiah.impl.postgresql.PgService
import com.urosjarc.dbmessiah.serializers.NumberTS
import kotlinx.datetime.Instant
import kotlinx.datetime.toJavaInstant
import kotlinx.datetime.toKotlinInstant
import java.sql.JDBCType
import java.sql.Timestamp
import kotlin.test.assertEquals
import kotlin.test.assertTrue


/**
 * For example we have table with column type Instant::class
 * The Instant::class serializer is not provided by the library since only basic kotlin JVM types serializers are defined...
 */
data class Parent8(var pk: Int? = null, val instant: Instant)

/**
 * All serializers that you can use are defined in com.urosjarc.dbmessiah.serializers
 * where you can look how they are defined. For learning purposes lets define our own
 * custom serializer for kotlinx Instant class to add support for our Table.time column.
 */

val instantTS: TypeSerializer<Instant> = TypeSerializer(
    kclass = Instant::class,        // Lets explain which kotlin class we want to serialize...
    dbType = "TIMESTAMP",           // Lets tell db type that will represent the value.
    jdbcType = JDBCType.TIMESTAMP,  // Lets also tell jdbc type for PreparedStatement in order to prevent SQL injections.

    /**
     * Note that 'rs' is JDBC ResultSet object which is object representing ONE row fetched from DB.
     * Because ResultSet is one row the columns are then fetched with 'i' index which represents index of the column to be fetched.
     * Correct index is passed by default to the user. The variable 'info' contains additional infos about the decoding value.
     *
     */
    decoder = { rs, i, info -> rs.getTimestamp(i).toInstant().toKotlinInstant() },  // Lets define how to decode value from result set...

    /**
     * Note that ps is JDBC PreparedStatement object which is object representing ONE SQL statement.
     * Variable 'i' represents the index of '?' for which 'x' value to be injected in the prepared statement.
     * Variable 'x' represents the actual value that will be injected instead of '?'.
     */
    encoder = { ps, i, x -> ps.setTimestamp(i, Timestamp.from(x.toJavaInstant())) } // Lets define how to encode value to prepared statement...
)

/**
 * In the following example we apply the Instant::class serializer to different levels of database.
 * In the beginning of database development you would want to define you serializers to globalSerializer,
 * after a while to optimize the database you would then define custom serializers to problematic objects.
 *
 * Here is an example how to apply Instant::class serializer to different database level
 */
val mainSchema4 = PgSchema(
    name = "main", tables = listOf(
        Table(
            primaryKey = Parent8::pk,
            serializers = listOf(
                instantTS // You can apply serializer to specific table which will override the same serializer inside schema and globalSerializers.
            ),
            columnSerializers = listOf(
                Parent8::instant to instantTS // You can apply serializer to specific column which will override the same serializer inside table, schema, globalSerializer.
            )
        )
    ),
    serializers = listOf(
        instantTS // You can apply serializer to specific schema which will override the same serializer inside globalSerializers.
    )
)

val postgresqlSerializer2 = PgSerializer(
    schemas = listOf(mainSchema4),
    globalSerializers = listOf( // One option is to define serializer to global serializer which will be applied to all database.
        NumberTS.int, // You can use allready defined serializer provided by the library...
        instantTS     // Here you defined your own serializer...
    )
)

val postgresqlService2 = PgService(config = config1, ser = postgresqlSerializer2)

fun main_008() {

    postgresqlService2.query {

        /**
         * Setup database
         */
        it.schema.create(schema = mainSchema4)
        it.table.dropCascade(table = Parent8::class)
        it.table.create(table = Parent8::class)

        /**
         * Insert table with custom tipe
         */
        val currentInstant = Instant.parse("2024-02-24T19:22:45Z")
        val parent80 = Parent8(instant = currentInstant)
        assertTrue(it.row.insert(parent80))

        /**
         * Get inserted element, and check if it matches with expected value.
         */
        val parent81 = it.row.select(table = Parent8::class, pk = parent80.pk!!)!!
        assertEquals(parent81, parent80)
        assertEquals(parent81.instant, parent80.instant)

    }


}
