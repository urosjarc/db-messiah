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
data class PgParent(
    var pk: Int? = null, // Primary keys should be mutable (since value will be set on insert action)...
    var value: String
)

data class PgChild(
    var pk: Int? = null, // Primary keys should be mutable (since value will be set on insert action)...
    val parent_pk: Int,  // Foreign key pointing to parent primary key...
    var value: String
)

/**
 * 2. Define your database schemas
 *
 * In sqlite there is no schema to be defined (sqlite does have schemas).
 * But in postgresql you have schemas so you have to define your tables to
 * appropriate schema and follow best practice for that specific database.
 */
val parent_schema = PgSchema(
    name = "parent_schema",
    tables = listOf(
        Table(PgParent::pk), //Pass primary key reference to table to register `Parent` as db table and mark `pk` as primary key.
    )
)

val child_schema = PgSchema(
    name = "child_schema", tables = listOf(
        Table(
            PgChild::pk, foreignKeys = listOf(
                PgChild::parent_pk to PgParent::class // Define mapping from foreign key to parent table.
            )
        )
    )
)

/**
 * 3. Create database serializer and explain database structure...
 *
 * Note that for different databases the Serializer API will differ since there are some differences between databases.
 * The same goes for the queries! Not all database will support some query functionality... The library tries to
 * adapt to those differences as much as possible to reflect supported functionality by the specific database.
 */
val pgSerializer = PgSerializer(
    schemas = listOf(parent_schema, child_schema),
    globalSerializers = AllTS.basic
)

/**
 * 4. Define HikariCP configuration...
 *
 * > https://github.com/brettwooldridge/HikariCP
 * > https://github.com/brettwooldridge/HikariCP?tab=readme-ov-file#rocket-initialization
 */
val pgConfig = Properties().apply {
    this["jdbcUrl"] = "jdbc:postgresql://localhost:5432/public"
    this["username"] = "root"
    this["password"] = "root"
}

/**
 * 5. Create database service by providing it your db serializer and db configuration...
 */
val postgresql = PgService(config = pgConfig, ser = pgSerializer)

fun main_001() {
    postgresql.query {
        /**
         * 6. Create schema
         */
        it.schema.create(schema = parent_schema)
        it.schema.create(schema = child_schema)

        /**
         * 7. Create tables
         */
        it.table.create(table = PgParent::class)
        it.table.create(table = PgChild::class)

        /**
         * 8. Make some batch inserts
         *
         * Note that batch inserts are highly optimized! If you don't care
         * to retrieve primary keys then batch operations are preferred!
         * Library will group batch operations in groups of 1000 and then execute
         * each group one by one in order to not block the database process.
         */
        val pgParents = MutableList(size = 100) { PgParent(value = "$it parent") }
        val numInserted = it.batch.insert(pgParents)
        assertEquals(numInserted, 100)

        /**
         * 9. Get all inserted rows since primary key is not retrieved in batch queries.
         */
        val newPgParents = it.table.select(table = PgParent::class)

        /**
         * 10. Batch update
         */
        newPgParents.forEach { it.value += " new" }
        val numUpdated = it.batch.update(newPgParents)
        assertEquals(numUpdated, 100)

        /**
         * 11. Select specific page with offset pagination.
         *
         * Note that this should be used for small tables.
         */
        val page0 = it.table.select(table = PgParent::class, page = Page(number = 3, orderBy = PgParent::pk, limit = 4))
        assertEquals(page0.size, 4)

        /**
         * 12. Select specific page with cursor pagination.
         *
         * Note that this should be used for big tables.
         */
        val page1 = it.table.select(table = PgParent::class, cursor = Cursor(index = 3, orderBy = PgParent::pk, limit = 4))
        assertEquals(page1.size, 4)

        /**
         * 13. Cursor pagination with better type safety.
         */
        val page2 = it.table.select(table = PgParent::class, cursor = Cursor(row = pgParents[0], orderBy = PgParent::pk, limit = 4))
        assertEquals(page2.size, 4)

        /**
         * 14. Batch delete
         */
        newPgParents.forEach { it.value += " new" }
        val numDeleted = it.batch.delete(newPgParents)
        assertEquals(numDeleted, 100)

        /**
         * 15. Drop tables
         */
        it.table.drop(table = PgChild::class)
        it.table.drop(table = PgParent::class)

        /**
         * 16. Drop schema
         */
        it.schema.drop(schema = parent_schema)
        it.schema.drop(schema = child_schema)

    }
}
