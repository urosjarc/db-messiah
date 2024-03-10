import com.urosjarc.dbmessiah.domain.Isolation
import com.urosjarc.dbmessiah.domain.Table
import com.urosjarc.dbmessiah.exceptions.ConnectionException
import com.urosjarc.dbmessiah.impl.sqlite.SqliteSerializer
import com.urosjarc.dbmessiah.impl.sqlite.SqliteService
import com.urosjarc.dbmessiah.serializers.AllTS
import org.junit.jupiter.api.assertThrows
import java.sql.SQLException
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Define your database
 */
data class Parent4(var pk: Int? = null, var value: String)

val service4 = SqliteService(
    config = config0,
    ser = SqliteSerializer(
        tables = listOf(Table(Parent4::pk)),
        globalSerializers = AllTS.basic,
    )
)

fun main_004() {
    service4.autocommit {
        it.table.drop<Parent4>()
        it.table.create<Parent4>()
    }

    /**
     * Database connection activated with "autocommit" will be by default non-transactional, with other words db connection will auto-commit changes.
     * This means that for every database call the changes will be reflected in the database right away without the possibility of reverting changes.
     * If you are performing some special actions for which you need to revert the changes you should use "transactional" database connection like so...
     */
    val exception0 = assertThrows<ConnectionException> {
        service4.transaction {
            it.row.insert(row = Parent4(value = "parent1")) // We insert one row to the database.
            throw Exception("Ops something went wrong!!!")  // But unfortunately somewhere inside our code the exception occurs.
        }
    }
    assertEquals(exception0.message, "Transaction was interrupted by unhandled exception, executing rollback")

    /**
     * Because in the previous transaction exception occurred the changes are reverted inside database and our table should be still empty!
     */
    service4.autocommit {
        assertTrue(it.table.select<Parent4>().isEmpty())
    }

    /**
     * Transactional database connections are joust like git commits but with the difference that db commits are PUSHED automatically with every commit.
     * When transactional connection is active all database calls will perform so-called db commit and then PUSH changes (commit) right away to the database!
     * It's important to note that this is difference between git commits and db commits. When you are using git commits changes are located only
     * in your local repository and if you like you can then push changes (commits) to the remote repository to synchronize the changes.
     * But when we use db commits the changes are pushed automatically to database!!!
     * If you roll back all the changes the db will joust like git, revert all commits performed on this specific connection back to the original state.
     * In case if you need to performed more complex operations you can create multiple save-points on which you can revert changes to...
     */
    service4.transaction {
        /**
         * Commits:
         *  - parent1
         */
        it.row.insert(row = Parent4(value = "parent1"))

        /**
         * Commits:
         *  - parent1
         *  - savepoint1
         */
        val savePoint1 = it.roolback.savePoint()

        /**
         * Commits:
         *  - parent1
         *  - savepoint1
         *  - parent2
         */
        it.row.insert(row = Parent4(value = "parent2"))

        /**
         * Commits:
         *  - parent1
         *  - savepoint0
         *  - parent2
         *  - savepoint1
         */
        val savePoint2 = it.roolback.savePoint()

        /**
         * Commits:
         *  - parent1
         *  - savepoint1
         *  - parent2
         *  - savepoint2
         *  - parent3
         */
        it.row.insert(row = Parent4(value = "parent3"))

        /**
         * Commits:
         *  - parent1
         */
        it.roolback.to(point = savePoint1)

        /**
         * At the current point we have only parent1 commit,
         * and this state is reflected in database while transaction is running!
         * Note that db commits are sent to database right away!
         */
        assertEquals(1, it.table.select<Parent4>().size)

        /**
         * If you at this point rollback changes to savepoint1
         * JDBC driver will raise Exception telling you that savepoint does not exists.
         */
        val exception1 = assertThrows<SQLException> {
            it.roolback.to(point = savePoint2)
        }

        /**
         * Similar exception message will be produced by other database drivers but with different names etc...
         */
        assertEquals(exception1.message, "[SQLITE_ERROR] SQL error or missing database (no such savepoint: SQLITE_SAVEPOINT_2)")
    }

    /**
     * If your application deals with multiple database connection running at the same time querying the database you would want to
     * set the isolation level for your transactions. Isolation defines how or when the changes made by one operation become visible to others.
     * > https://en.wikipedia.org/wiki/Isolation_(database_systems)
     * If you need to specificaly define isolation level you can do it like so...
     */
    service4.transaction(isolation = Isolation.READ_UNCOMMITTED) {
        // your operations
    }

}
