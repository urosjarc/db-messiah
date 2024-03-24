package basic_postgresql

import com.urosjarc.dbmessiah.domain.Cursor
import com.urosjarc.dbmessiah.domain.Page
import com.urosjarc.dbmessiah.domain.Table
import com.urosjarc.dbmessiah.impl.postgresql.PgSchema
import com.urosjarc.dbmessiah.impl.postgresql.PgSerializer
import com.urosjarc.dbmessiah.impl.postgresql.PgService
import com.urosjarc.dbmessiah.serializers.BasicTS
import java.util.*
import kotlin.test.assertEquals


/**
 * Define your domain classes (tables)...
 */
data class Parent(var pk: Int? = null, var value: String)
data class Child(var pk: Int? = null, val parent_pk: Int, var value: String)

/**
 * Define your database schemas
 * In sqlite there is no schema to be defined (sqlite does not have schemas).
 * But in postgresql you have schemas, so you have to define your tables to
 * appropriate schema.
 */
val parent_schema = PgSchema(
    name = "parent_schema",
    tables = listOf(
        Table(Parent::pk),
    )
)

val child_schema = PgSchema(
    name = "child_schema", tables = listOf(
        Table(
            Child::pk, foreignKeys = listOf(
                Child::parent_pk to Parent::class
            )
        )
    )
)

/**
 * Create database service...
 * Note that for different databases the Serializer API will differ since there are some differences between databases.
 * The same goes for the queries. Not all database will support some query functionality... The library tried to
 * adapt to those differences as much as possible in order to reflect supported functionality by the specific database.
 */
val serializer = PgSerializer(
    schemas = listOf(parent_schema, child_schema),
    globalSerializers = BasicTS.postgresql
)

val config = Properties().apply {
    this["jdbcUrl"] = "jdbc:postgresql://localhost:5432/public"
    this["username"] = "root"
    this["password"] = "root"
}

val service = PgService(config = config, ser = serializer)

fun basic_postgresql() {
    service.autocommit {
        /**
         * Create schema
         * Note that nothing in this library will be created by it self only primary keys on insertion!
         */
        it.schema.create(schema = parent_schema) // We have defined schema as variable so that we can use it in tape safe maner.
        it.schema.create(schema = child_schema)  // We have defined schema as variable so that we can use it in tape safe maner.

        /**
         * Create tables and reset them
         */
        it.table.create<Parent>()
        it.table.create<Child>()
        it.table.delete<Parent>()
        it.table.delete<Child>()

        /**
         * Make some batch inserts
         * Note that batch inserts are highly optimized! If you don't care
         * to retrieve primary keys then batch operations are preferred!
         * Library will group batch operations in groups of 1000 and then execute
         * each group one by one in order to not block the database process.
         */
        val parent0 = MutableList(size = 100) { Parent(value = "$it parent") }
        it.batch.insert(parent0)

        /**
         * Get all inserted rows since primary key is not retrieved in batch queries.
         */
        val parents0 = it.table.select<Parent>()

        /**
         * Batch update
         */
        parents0.forEach { it.value += " new" }
        it.batch.update(parents0)

        /**
         * Select specific page with offset pagination.
         * Note that this should be used for small tables.
         */
        val page0 = it.table.select<Parent>(page = Page(number = 3, orderBy = Parent::pk, limit = 4))
        assertEquals(page0.size, 4)

        /**
         * Select specific page with cursor pagination.
         * Note that this should be used for big tables.
         */
        val page1 = it.table.select<Parent, Int>(cursor = Cursor(index = 3, orderBy = Parent::pk, limit = 4))
        assertEquals(page1.size, 4)

        /**
         * Batch delete
         */
        parents0.forEach { it.value += " new" }
        it.batch.delete(parents0)

        /**
         * Drop tables (with cascading)
         */
        it.table.dropCascade<Child>()
        it.table.dropCascade<Parent>()

        /**
         * Drop schema
         */
        it.schema.drop(schema = parent_schema)
        it.schema.drop(schema = child_schema)
    }
}
