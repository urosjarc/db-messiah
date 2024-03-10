import com.urosjarc.dbmessiah.domain.Cursor
import com.urosjarc.dbmessiah.domain.Page
import com.urosjarc.dbmessiah.domain.Table
import com.urosjarc.dbmessiah.impl.postgresql.PgSchema
import com.urosjarc.dbmessiah.impl.postgresql.PgSerializer
import com.urosjarc.dbmessiah.impl.postgresql.PgService
import com.urosjarc.dbmessiah.serializers.AllTS
import java.util.*
import kotlin.test.assertEquals


/**
 * Define your domain classes (tables)...
 */
data class Parent1(
    var pk: Int? = null,
    var value: String
)

data class Child1(
    var pk: Int? = null,
    val parent_pk: Int,
    var value: String
)

/**
 * Define your database schemas
 * In sqlite there is no schema to be defined (sqlite does have schemas).
 * But in postgresql you have schemas so you have to define your tables to
 * appropriate schema and follow best practice for that specific database.
 */
val parent1_schema = PgSchema(
    name = "parent1_schema",
    tables = listOf(
        Table(Parent1::pk),
    )
)

val child1_schema = PgSchema(
    name = "child1_schema", tables = listOf(
        Table(
            Child1::pk, foreignKeys = listOf(
                Child1::parent_pk to Parent1::class
            )
        )
    )
)

/**
 * Create database service...
 * Note that for different databases the Serializer API will differ since there are some differences between databases.
 * The same goes for the queries! Not all database will support some query functionality... The library tries to
 * adapt to those differences as much as possible to reflect supported functionality by the specific database.
 */
val ser1 = PgSerializer(
    schemas = listOf(parent1_schema, child1_schema),
    globalSerializers = AllTS.basic
)

val config1 = Properties().apply {
    this["jdbcUrl"] = "jdbc:postgresql://localhost:5432/public"
    this["username"] = "root"
    this["password"] = "root"
}

val service1 = PgService(config = config1, ser = ser1)

fun main_001() {
    service1.autocommit {
        /**
         * Create schema
         * Note that nothing in this library will be created by it self only primary keys on insertion!
         */
        it.schema.create(schema = parent1_schema) // We have defined schema as variable so that we can use it in tape safe maner.
        it.schema.create(schema = child1_schema)  // We have defined schema as variable so that we can use it in tape safe maner.

        /**
         * Create tables and reset them
         */
        it.table.create<Parent1>()
        it.table.create<Child1>()
        it.table.delete<Parent1>()
        it.table.delete<Child1>()

        /**
         * Make some batch inserts
         * Note that batch inserts are highly optimized! If you don't care
         * to retrieve primary keys then batch operations are preferred!
         * Library will group batch operations in groups of 1000 and then execute
         * each group one by one in order to not block the database process.
         */
        val parent0 = MutableList(size = 100) { Parent1(value = "$it parent") }
        it.batch.insert(parent0)

        /**
         * Get all inserted rows since primary key is not retrieved in batch queries.
         */
        val parents0 = it.table.select<Parent1>()

        /**
         * Batch update
         */
        parents0.forEach { it.value += " new" }
        it.batch.update(parents0)

        /**
         * Select specific page with offset pagination.
         * Note that this should be used for small tables.
         */
        val page0 = it.table.select<Parent1>(page = Page(number = 3, orderBy = Parent1::pk, limit = 4))
        assertEquals(page0.size, 4)

        /**
         * Select specific page with cursor pagination.
         * Note that this should be used for big tables.
         */
        val page1 = it.table.select<Parent1, Int>(cursor = Cursor(index = 3, orderBy = Parent1::pk, limit = 4))
        assertEquals(page1.size, 4)

        /**
         * Batch delete
         */
        parents0.forEach { it.value += " new" }
        it.batch.delete(parents0)

        /**
         * Drop tables (with cascading)
         */
        it.table.dropCascade<Child1>()
        it.table.dropCascade<Parent1>()

        /**
         * Drop schema
         */
        it.schema.drop(schema = parent1_schema)
        it.schema.drop(schema = child1_schema)
    }
}
