import com.urosjarc.dbmessiah.domain.Table
import com.urosjarc.dbmessiah.impl.sqlite.SqliteSerializer
import com.urosjarc.dbmessiah.impl.sqlite.SqliteService
import com.urosjarc.dbmessiah.serializers.AllTS
import java.util.*
import kotlin.test.*


/**
 * 1. Define your domain classes (tables)...
 */
data class Parent(
    var pk: Int? = null, // Primary keys should be mutable (since value will be set on insert action)...
    var value: String
)

data class Child(
    var pk: Int? = null, // Primary keys should be mutable (since value will be set on insert action)...
    val parent_pk: Int,  // Foreign key pointing to parent primary key...
    var value: String
)

/**
 * 2. Create database serializer and explain database structure...
 */
val serializer = SqliteSerializer(
    tables = listOf(
        Table(Parent::pk), //Pass primary key reference to table to register `Parent` as db table and mark `pk` as primary key.
        Table(
            Child::pk, foreignKeys = listOf(
                Child::parent_pk to Parent::class // Define mapping from foreign key to parent table.
            )
        ),
    ),
    globalSerializers = AllTS.basic //Pass list of all basic kotlin serializers to be used by serializer (PS: Its super easy to define your own custom serializer).
)

/**
 * 3. Define HikariCP configuration...
 *
 * > https://github.com/brettwooldridge/HikariCP
 * > https://github.com/brettwooldridge/HikariCP?tab=readme-ov-file#rocket-initialization
 */
val config = Properties().apply {
    this["jdbcUrl"] = "jdbc:sqlite::memory:"
}

/**
 * 4. Create database service by providing it your db serializer and db configuration...
 */
val sqlite = SqliteService(config = config, ser = serializer)

fun main() {
    sqlite.query { // Fetch available non-transactional connection from HikariCP connection pool ...
        /**
         * 5. Create table...
         */
        it.table.create(table = Parent::class) // Create new table...

        /**
         * 6. Insert row...
         */
        val parent = Parent(value = "Hello World")    // Create new object...
        val isInserted = it.row.insert(row = parent)  // Insert object to table...
        assertTrue(isInserted)                        // Check if object was inserted...
        assertNotNull(parent.pk)                      // Check if object has primary key...

        /**
         * 7. Select all table elements
         */
        val parents0 = it.table.select(table = Parent::class) // SELECT all table elements...
        assertContains(parents0, parent)                      // Parent is contained in selected elements...

        /**
         * 8. Update row
         */
        parent.value = "Hello Space" // Change value or the object (row)...
        it.row.update(row = parent)  // Send this change to the database...

        /**
         * 9. Check if change was updated
         */
        val ele2 = it.row.select(table = Parent::class, pk = parent.pk!!) // Get specific row from the table by primary key...
        assertEquals(ele2, parent)                                         // Parent is equal to original...

        /**
         * 10. Remove row
         */
        val isDeleted = it.row.delete(row = parent) // Delete row from database
        assertTrue(isDeleted)                       // Check if object was deleted...
        assertNull(parent.pk)                       // Check if objects primary key was reseted on null...

        /**
         * 11. Insert many rows
         *
         * Note that inserting rows will create database call for each row and another call for fetching primary key.
         * So if you are inserting 3 rows to database there there will be 6 database calls in the worst case.
         * If database driver supports fetching primary key there will be only 3 database calls.
         */
        val parents1 = listOf(
            Parent(value = "parent10"),
            Parent(value = "parent11"),
            Parent(value = "parent12"),
        )
        val areInserted = it.row.insert(parents1)           // Insert all parents one by one...
        assertFalse(areInserted.contains(false))            // Check if list of bools does not have false values (all are inserted)...
        assertFalse(parents1.map { it.pk }.contains(null))  // All primary keys should be setted by the library...

        /**
         * 12. Update many rows
         */
        parents1.forEach { it.value += "_new" }
        val areUpdated = it.row.update(parents1)
        assertFalse(areUpdated.contains(false))   // Check if list of bools does not have false values (all are updated)...

        /**
         * 13. Remove many rows
         */
        val areDeleted = it.row.delete(parents1) // Delete all parents...
        assertFalse(areDeleted.contains(false))  // Check if list of bools does not have false values (all are updated)...
        parents1.forEach { assertNull(it.pk) }   // All primary keys should be reseted by the library...

        /**
         * 11. Drop table
         */
        it.table.drop(table = Parent::class) // Drop table from database
    }
}
