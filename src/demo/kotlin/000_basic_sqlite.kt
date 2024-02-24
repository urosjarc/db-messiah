import com.urosjarc.dbmessiah.domain.Table
import com.urosjarc.dbmessiah.impl.sqlite.SqliteSerializer
import com.urosjarc.dbmessiah.impl.sqlite.SqliteService
import com.urosjarc.dbmessiah.serializers.AllTS
import java.util.*
import kotlin.test.*


/**
 * Define your domain classes (tables)...
 */
data class Parent0(
    var pk: Int? = null, // Primary keys should be mutable (since value will be set on insert action)...
    var value: String
)

data class Child0(
    var pk: Int? = null, // Primary keys should be mutable (since value will be set on insert action)...
    val parent_pk: Int,  // Foreign key pointing to parent primary key...
    var value: String
)

/**
 * Create database serializer and explain database structure...
 */
val sqliteSerializer0 = SqliteSerializer(
    tables = listOf(
        Table(Parent0::pk), //Pass primary key reference to table to register `Parent` as db table and mark `pk` as primary key.
        Table(
            Child0::pk, foreignKeys = listOf(
                Child0::parent_pk to Parent0::class // Define mapping from foreign key to parent table.
            )
        ),
    ),
    globalSerializers = AllTS.basic //Pass list of all basic kotlin serializers to be used by serializer (PS: Its super easy to define your own custom serializer).
)

/**
 * Define HikariCP configuration...
 * > https://github.com/brettwooldridge/HikariCP
 * > https://github.com/brettwooldridge/HikariCP?tab=readme-ov-file#rocket-initialization
 */
val sqliteConfig = Properties().apply {
    this["jdbcUrl"] = "jdbc:sqlite::memory:"
}

/**
 * Create database service by providing it your db serializer and db configuration...
 */
val sqliteService0 = SqliteService(config = sqliteConfig, ser = sqliteSerializer0)

fun main_000() {
    sqliteService0.query { // Fetch available non-transactional connection from HikariCP connection pool ...
        /**
         * Create table...
         */
        it.table.create(table = Parent0::class) // Create new table...

        /**
         * Insert row...
         */
        val parent0 = Parent0(value = "Hello World")    // Create new object...
        val isInserted = it.row.insert(row = parent0)  // Insert object to table...
        assertTrue(isInserted)                        // Check if object was inserted...
        assertNotNull(parent0.pk)                      // Check if object has primary key...

        /**
         * Select all table elements
         */
        val parents0 = it.table.select(table = Parent0::class) // SELECT all table elements...
        assertContains(parents0, parent0)                      // Parent is contained in selected elements...

        /**
         * Update row
         */
        parent0.value = "Hello Space" // Change value or the object (row)...
        it.row.update(row = parent0)  // Send this change to the database...

        /**
         * Check if change was updated
         */
        val parent1 = it.row.select(table = Parent0::class, pk = parent0.pk!!) // Get specific row from the table by primary key...
        assertEquals(parent1, parent0)                                        // Parent is equal to original...

        /**
         * Remove row
         */
        val isDeleted = it.row.delete(row = parent0) // Delete row from database
        assertTrue(isDeleted)                       // Check if object was deleted...
        assertNull(parent0.pk)                       // Check if objects primary key was reseted on null...

        /**
         * Insert many rows
         * Note that inserting rows will create database call for each row and another call for fetching primary key.
         * So if you are inserting 3 rows to database there there will be 6 database calls in the worst case.
         * If database driver supports fetching primary key there will be only 3 database calls.
         */
        val parents1 = listOf(
            Parent0(value = "parent10"),
            Parent0(value = "parent11"),
            Parent0(value = "parent12"),
        )
        val areInserted = it.row.insert(parents1)           // Insert all parents one by one...
        assertFalse(areInserted.contains(false))            // Check if list of bools does not have false values (all are inserted)...
        assertFalse(parents1.map { it.pk }.contains(null))  // All primary keys should be setted by the library...

        /**
         * Update many rows
         */
        parents1.forEach { it.value += "_new" }
        val areUpdated = it.row.update(parents1)
        assertFalse(areUpdated.contains(false))   // Check if list of bools does not have false values (all are updated)...

        /**
         * Remove many rows
         */
        val areDeleted = it.row.delete(parents1) // Delete all parents...
        assertFalse(areDeleted.contains(false))  // Check if list of bools does not have false values (all are updated)...
        parents1.forEach { assertNull(it.pk) }   // All primary keys should be reseted by the library...

        /**
         * Delete all table rows...
         */
        val numDeleted = it.table.delete(table = Parent0::class)
        assertEquals(numDeleted, 0)                             // Num deleted should be 0 since there was no children in the table

        /**
         * Drop table
         */
        it.table.drop(table = Child0::class)  // Drop table from database
        it.table.drop(table = Parent0::class) // Drop table from database
    }
}
