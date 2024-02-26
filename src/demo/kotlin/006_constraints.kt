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
 */
val ser6 = SqliteSerializer(
    tables = listOf(
        Table(
            Parent6::pk, constraints = listOf(
                Parent6::uniqueValue to listOf(C.UNIQUE) // We will attach unique modifier to the column...
            )
        ),
    ), globalSerializers = AllTS.basic
)

/**
 * Create database service by providing it your db serializer and db configuration...
 */
val service6 = SqliteService(config = config0, ser = ser6)

fun main_006() {
    service6.autocommit {
        it.table.create(Parent6::class)

        val isInserted = it.row.insert(row = Parent6(uniqueValue = "asdf"))
        assertTrue(isInserted)

        val exception = assertThrows<Throwable> {
            it.row.insert(row = Parent6(uniqueValue = "asdf"))
        }

        assertContains(
            exception.stackTraceToString(),
            "[SQLITE_CONSTRAINT_UNIQUE] A UNIQUE constraint failed (UNIQUE constraint failed: Parent6.uniqueValue)"
        )


    }
}
