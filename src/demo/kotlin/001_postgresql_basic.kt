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
 * 1. Define your domain classes (tables)...
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
 * 2. Define your database schemas
 *
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
 * 3. Create database service...
 *
 * Note that for different databases the Serializer API will differ since there are some differences between databases.
 * The same goes for the queries! Not all database will support some query functionality... The library tries to
 * adapt to those differences as much as possible to reflect supported functionality by the specific database.
 */
val pgSerializer0 = PgSerializer(
    schemas = listOf(parent1_schema, child1_schema),
    globalSerializers = AllTS.basic
)

val postgresqlConfig = Properties().apply {
    this["jdbcUrl"] = "jdbc:postgresql://localhost:5432/public"
    this["username"] = "root"
    this["password"] = "root"
}

val pgService0 = PgService(config = postgresqlConfig, ser = pgSerializer0)

fun main_001() {
    pgService0.query {
        /**
         * 4. Create schema
         *
         * Note that nothing in this library will be created by it self
         */
        it.schema.create(schema = parent1_schema) // We have defined schema as variable so that we can use it in tape safe maner.
        it.schema.create(schema = child1_schema)  // We have defined schema as variable so that we can use it in tape safe maner.

        /**
         * 5. Create tables and reset them
         */
        it.table.create(table = Parent1::class)
        it.table.create(table = Child1::class)
        it.table.delete(table = Parent1::class)
        it.table.delete(table = Child1::class)

        /**
         * 6. Make some batch inserts
         *
         * Note that batch inserts are highly optimized! If you don't care
         * to retrieve primary keys then batch operations are preferred!
         * Library will group batch operations in groups of 1000 and then execute
         * each group one by one in order to not block the database process.
         */
        val pgParents = MutableList(size = 100) { Parent1(value = "$it parent") }
        val numInserted = it.batch.insert(pgParents)
        assertEquals(numInserted, 100)

        /**
         * 7. Get all inserted rows since primary key is not retrieved in batch queries.
         */
        val newPgParents = it.table.select(table = Parent1::class)

        /**
         * 8. Batch update
         */
        newPgParents.forEach { it.value += " new" }
        val numUpdated = it.batch.update(newPgParents)
        assertEquals(numUpdated, 100)

        /**
         * 9. Select specific page with offset pagination.
         *
         * Note that this should be used for small tables.
         */
        val page0 = it.table.select(table = Parent1::class, page = Page(number = 3, orderBy = Parent1::pk, limit = 4))
        assertEquals(page0.size, 4)

        /**
         * 10. Select specific page with cursor pagination.
         *
         * Note that this should be used for big tables.
         */
        val page1 = it.table.select(table = Parent1::class, cursor = Cursor(index = 3, orderBy = Parent1::pk, limit = 4))
        assertEquals(page1.size, 4)

        /**
         * 11. Cursor pagination with better type safety.
         */
        val page2 = it.table.select(table = Parent1::class, cursor = Cursor(row = page1[0], orderBy = Parent1::pk, limit = 4))
        assertEquals(page2.size, 4)

        /**
         * 12. Batch delete
         */
        newPgParents.forEach { it.value += " new" }
        val numDeleted = it.batch.delete(newPgParents)
        assertEquals(numDeleted, 100)

        /**
         * 13. Drop tables (with cascading)
         */
        it.table.dropCascade(table = Child1::class)
        it.table.dropCascade(table = Parent1::class)

        /**
         * 14. Drop schema
         */
        it.schema.drop(schema = parent1_schema)
        it.schema.drop(schema = child1_schema)

    }
}
