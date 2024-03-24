package custom_type_serializers

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
import java.util.*
import kotlin.test.assertEquals


/**
 * For example we have table with column type Instant::class
 * The Instant::class serializer is not provided by the library since only basic kotlin JVM types serializers are defined...
 */
data class Parent(var pk: Int? = null, val instant: Instant)

/**
 * All serializers that you can use are defined in com.urosjarc.dbmessiah.serializers
 * where you can look how they are defined. For learning purposes lets define our own
 * custom serializer for kotlinx Instant class to add support for our Table.time column.
 * Inside constructor parameters there are extensive comments explaning everything...
 */

val instantTS: TypeSerializer<Instant> = TypeSerializer(
    kclass = Instant::class,        // Which kotlin class we want to serialize...
    dbType = "TIMESTAMP",           // Which db type will represent the value.
    jdbcType = JDBCType.TIMESTAMP,  // Which jdbc type will be used for PreparedStatement in order to prevent SQL injections.

    /**
     * Note that 'rs' is JDBC ResultSet object which is object representing ONE row fetched from DB.
     * Because ResultSet is one row the columns are then fetched with 'i' index which represents index of the column to be fetched.
     * Correct index is passed by default to the user. The variable 'info' contains additional infos about the decoding value.
     */
    decoder = { rs, i, _ -> rs.getTimestamp(i).toInstant().toKotlinInstant() },  // Lets define how to decode value from result set...

    /**
     * Note that ps is JDBC PreparedStatement object which is object representing ONE SQL statement.
     * Variable 'i' represents the index of '?' for which 'x' value to be injected in the prepared statement.
     * Variable 'x' represents the actual value that will be injected instead of '?'.
     */
    encoder = { ps, i, x -> ps.setTimestamp(i, Timestamp.from(x.toJavaInstant())) } // Lets define how to encode value to prepared statement...
)

/**
 * There are multiple levels on which we can apply specific type serializer.
 * There are (global, schema, table, column) levels listed by increasing priority.
 * If type serializer is applied on global level all columns over all schemas will be serialized with this serializer.
 * If type serializer is applied on schema level then only those columns defined in that schemas will be serialized with this serializer.
 * The same is true for other levels. If system finds multiple serializer for some column then system will use the type serializer with highest priority.
 * Here is an example how to apply Instant::class serializer to different database levels.
 */
val schema = PgSchema(
    name = "custom_type_serializers", tables = listOf(
        Table(
            primaryKey = Parent::pk,
            serializers = listOf(
                instantTS // You can apply serializer to specific table which will override the same serializer inside schema and globalSerializers.
            ),
            columnSerializers = listOf(
                Parent::instant to instantTS // You can apply serializer to specific column which will override the same serializer inside table, schema, globalSerializer.
            )
        )
    ),
    serializers = listOf(
        instantTS // You can apply serializer to specific schema which will override the same serializer inside globalSerializers.
    )
)

val serializer = PgSerializer(
    schemas = listOf(schema),
    globalSerializers = listOf( // One option is to define serializer to global serializer which will be applied to all database.
        NumberTS.int, // You can use allready defined serializer provided by the library...
        instantTS     // Here you defined your own serializer...
    )
)

val service = PgService(ser = serializer, config = Properties().apply {
    this["jdbcUrl"] = "jdbc:postgresql://localhost:5432/public"
    this["username"] = "root"
    this["password"] = "root"
})

fun custom_type_serializers() {

    service.autocommit {

        /**
         * Setup database
         */
        it.schema.create(schema = schema)
        it.table.dropCascade<Parent>()
        it.table.create<Parent>()

        /**
         * Insert table with custom tipe
         */
        val currentInstant = Instant.parse("2024-02-24T19:22:45Z")
        val parent0 = Parent(instant = currentInstant)
        it.row.insert(parent0)

        /**
         * Get inserted element, and check if it matches with expected value.
         */
        val parent1 = it.row.select<Parent>(pk = parent0.pk!!)!!
        assertEquals(parent1, parent0)
        assertEquals(parent1.instant, parent0.instant)

    }


}
