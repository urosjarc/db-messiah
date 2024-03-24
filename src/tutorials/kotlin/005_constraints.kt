package constraints

import com.urosjarc.dbmessiah.domain.C
import com.urosjarc.dbmessiah.domain.Table
import com.urosjarc.dbmessiah.impl.sqlite.SqliteSerializer
import com.urosjarc.dbmessiah.impl.sqlite.SqliteService
import com.urosjarc.dbmessiah.serializers.BasicTS
import org.junit.jupiter.api.assertThrows
import java.util.*
import kotlin.test.assertContains


/**
 * NOT NULL constraint will be inferred from you domain class.
 * All types ending with question mark ? will be allowed inside database
 * to store NULL value. All types without ending with question mark will
 * have NOT NULL constraint. This constraint is enforced meaning
 * user will have to accept this opinionated behaviour. Domain classes will
 * enforce the state inside database at all time. Domain space is our only source of
 * truth and database MUST reflect that back like a mirror without distortions!
 */
data class Parent(
    var pk: Int? = null,
    var uniqueValue: String, // Any column that is subtype of Any will be marked as non nullable column.
    var nullableValue: String? // Any column that is subtype of Any? will be marked as column that can be nullable.
)

/**
 * Create database serializer and explain database structure...
 * Each table accepts list of column mapping to its column constraints.
 */
val serializer = SqliteSerializer(
    tables = listOf(
        Table(
            /**
             * If primary key is mutable system will automatically mark primary key as auto-incremental.
             * If mutable primary key has defined constraints and you forgot to add auto-incremental constraint to it,
             * system will warn you with the error since you are not allowed to have primary key as null.
             * If primary key is immutable system will leave PK initialization to the user!
             * Note that if you are matching inappropriate constraint to the column system will
             * warn you at system initialization. Some databases will not support some constraint,
             * system will try to catch any abnormal configuration from user and him while serializer is constructing.
             * For example you can't mark normal column to be of type CASCADE_DELETE or CASCADE_UPDATE the sistem will throw
             * warning exception back to the user.
             */
            Parent::pk,
            constraints = listOf(
                Parent::uniqueValue to listOf(C.UNIQUE) // We will attach unique modifier to the column...
            )
        ),
    ), globalSerializers = BasicTS.basic
)

/**
 * Create standard database service...
 */
val service = SqliteService(ser = serializer, config = Properties().apply { this["jdbcUrl"] = "jdbc:sqlite::memory:" })

fun constraints() {
    service.autocommit {
        /**
         * Setup database arhitecture...
         */
        it.table.create<Parent>()

        /**
         * Insert some rows with unique columns..
         */
        it.row.insert(row = Parent(uniqueValue = "asdf", nullableValue = null))

        /**
         * Lets test if column can be inserted again with the same unique value...
         * And lets test if we can insert nullable value.
         */
        val exception = assertThrows<Throwable> {
            it.row.insert(row = Parent(uniqueValue = "asdf", nullableValue = null))
        }

        /**
         * We can see that constraint is working...
         */
        assertContains(
            exception.stackTraceToString(),
            "[SQLITE_CONSTRAINT_UNIQUE] A UNIQUE constraint failed (UNIQUE constraint failed: Parent.uniqueValue)"
        )


    }
}
