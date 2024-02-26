import com.urosjarc.dbmessiah.domain.C
import com.urosjarc.dbmessiah.domain.Table
import com.urosjarc.dbmessiah.impl.sqlite.SqliteSerializer
import com.urosjarc.dbmessiah.impl.sqlite.SqliteService
import com.urosjarc.dbmessiah.serializers.AllTS
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertContains
import kotlin.test.assertTrue


/**
 * Define your domain classes (tables)...
 */
data class Parent6(
    var pk: Int? = null,
    var uniqueValue: String
)

/**
 * Create database serializer and explain database structure...
 * Each table accepts list of column mapping to its column constraints.
 */
val ser6 = SqliteSerializer(
    tables = listOf(
        Table(
            /**
             * If primary key is mutable system will automatically mark primary key as auto-incremental.
             * If mutable primary key has defined constraints and you forgot to add auto-incremental constraint to it,
             * system will warn you with the error since you are not allowed to have primary key as null.
             * If primary key is immutable system will leave PK initialization to the user!
             * Note that if you are matching inappropriate constraint to the column system will
             * warn you at system initialization.
             */
            Parent6::pk,
            constraints = listOf(
                Parent6::uniqueValue to listOf(C.UNIQUE) // We will attach unique modifier to the column...
            )
        ),
    ), globalSerializers = AllTS.basic
)

/**
 * Create standard database service...
 */
val service6 = SqliteService(config = config0, ser = ser6)

fun main_006() {
    service6.autocommit {
        /**
         * Setup database arhitecture...
         */
        it.table.create(Parent6::class)

        /**
         * Insert some rows with unique columns..
         */
        val isInserted = it.row.insert(row = Parent6(uniqueValue = "asdf"))
        assertTrue(isInserted)

        /**
         * Lets test if column can be inserted again with the same unique value...
         */
        val exception = assertThrows<Throwable> {
            it.row.insert(row = Parent6(uniqueValue = "asdf"))
        }

        /**
         * We can see that constraint is working...
         */
        assertContains(
            exception.stackTraceToString(),
            "[SQLITE_CONSTRAINT_UNIQUE] A UNIQUE constraint failed (UNIQUE constraint failed: Parent6.uniqueValue)"
        )


    }
}
