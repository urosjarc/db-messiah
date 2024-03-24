package basic_sqlite

import com.urosjarc.dbmessiah.domain.Table
import com.urosjarc.dbmessiah.impl.sqlite.SqliteSerializer
import com.urosjarc.dbmessiah.impl.sqlite.SqliteService
import com.urosjarc.dbmessiah.serializers.BasicTS
import java.io.File
import java.util.*
import kotlin.test.*


/**
 * Define your domain classes (tables)...
 */
data class Parent(
    var pk: Int? = null, // Auto-incremental primary key (Int?, Uint?)
    var value: String
)

data class Child(
    val pk: Int,         // Non auto-incremental primary key
    val parent_pk: Int,
    var value: String
)

/**
 * Create database serializer and explain database structure...
 * Note that every table MUST have primary key, tables without primary key are considered a bad practice and they are not supported by this library.
 */
val serializer = SqliteSerializer(
    tables = listOf(
        Table(Parent::pk), // Pass primary key reference to table to register `Parent` as db table and automatically mark `pk` as primary key.
        Table(
            Child::pk, foreignKeys = listOf(
                Child::parent_pk to Parent::class // Define mapping from foreign key to parent table.
            )
        ),
    ),
    globalSerializers = BasicTS.sqlite //Pass list of all basic kotlin serializers to be used by serializer (PS: Its super easy to define your own custom serializer).
)

/**
 * Define HikariCP configuration, for each database the minimal requirements are ("jdbcUrl", "username", "password")
 * For more custom settings and connection pooling please referee to official HikariCP library...
 * > https://github.com/brettwooldridge/HikariCP
 * > https://github.com/brettwooldridge/HikariCP?tab=readme-ov-file#rocket-initialization
 */
val config = Properties().apply {
    this["jdbcUrl"] = "jdbc:sqlite::memory:"
    this["username"] = "root"
    this["password"] = "root"
}

/**
 * Create database service by providing it your db serializer and db configuration...
 */
val service = SqliteService(config = config, ser = serializer)

fun basic_sqlite() {
    /**
     * To visualize database structure you can use plantUML extractor...
     * IntelliJ has plugin to support ".plantuml" extensions.
     * > https://plugins.jetbrains.com/plugin/7017-plantuml-integration
     * For more informations visit plantUML website and plugin for Intelij:
     * > https://plantuml.com/
     */
    File("db.plantuml").writeText(serializer.plantUML())

    /**
     *  When you want to query database, you will be provided with available db connection (SqliteQueryConn) from the connection pool.
     *  Creating new database connection is expensive operation that's why db-messiah uses
     *  HikariCP which stands for (Lightning fast Connection Pool).
     *  This library will create a pool of ~10 database connections when db service is created and then reuse them
     *  when needed. This means lightning fast db operations! Please refer to the HikariCP benchmarks...
     *  > https://github.com/brettwooldridge/HikariCP-benchmark
     */
    service.autocommit { dbConn: SqliteService.Connection -> // Fetch available (non-transactional) db connection from the HikariCP connection pool...
        /**
         * Create table...
         */
        dbConn.table.create<Parent>() // Create new table...

        /**
         * Insert row...
         */
        val parent0 = Parent(value = "Hello World") // Create new object...
        dbConn.row.insert(row = parent0)             // Insert object to table...
        assertNotNull(parent0.pk)                    // Check if object has primary key...

        /**
         * Select all table elements
         */
        val parents0 = dbConn.table.select<Parent>() // SELECT all table elements...
        assertContains(parents0, parent0)             // Parent is contained in selected elements...

        /**
         * Update row
         */
        parent0.value = "Hello Space"    // Change value or the object (row)...
        dbConn.row.update(row = parent0) // Send this change to the database...

        /**
         * Check if change was updated
         */
        val parent1 = dbConn.row.select<Parent>(pk = parent0.pk!!) // Get specific row from the table by primary key...
        assertEquals(parent1, parent0)   // Parent is equal to original...

        /**
         * Remove row
         */
        dbConn.row.delete(row = parent0) // Delete row from database
        assertNull(parent0.pk)           // Check if objects primary key was reseted on null...

        /**
         * Insert many rows
         */
        val parents1 = listOf(
            Parent(value = "parent10"),
            Parent(value = "parent11"),
            Parent(value = "parent12"),
        )
        dbConn.row.insert(parents1)                        // Insert all parents one by one...
        assertFalse(parents1.map { it.pk }.contains(null)) // All primary keys should be set by the library...

        /**
         * Update many rows
         */
        parents1.forEach { it.value += "_new" }
        dbConn.row.update(parents1)

        /**
         * Remove many rows
         */
        dbConn.row.delete(parents1)            // Delete all parents...
        parents1.forEach { assertNull(it.pk) } // All primary keys should be reseted by the library...

        /**
         * Delete all table rows...
         */
        dbConn.table.delete<Parent>()

        /**
         * Drop table
         */
        dbConn.table.drop<Child>()  // Drop table from database
        dbConn.table.drop<Parent>() // Drop table from database
    }
}
