package transactions

import com.urosjarc.dbmessiah.domain.Isolation
import com.urosjarc.dbmessiah.domain.Table
import com.urosjarc.dbmessiah.exceptions.ConnectionException
import com.urosjarc.dbmessiah.impl.sqlite.SqliteSerializer
import com.urosjarc.dbmessiah.impl.sqlite.SqliteService
import com.urosjarc.dbmessiah.serializers.AllTS
import org.junit.jupiter.api.assertThrows
import java.sql.SQLException
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Define your database
 */
data class Parent(var pk: Int? = null, var value: String)

val service = SqliteService(
    config = Properties().apply { this["jdbcUrl"] = "jdbc:sqlite::memory:" },
    ser = SqliteSerializer(tables = listOf(Table(Parent::pk)), globalSerializers = AllTS.sqlite)
)

fun transactions() {
    /**
     * Prepare database
     */
    service.autocommit {
        it.table.drop<Parent>()
        it.table.create<Parent>()
    }

    /**
     * Database connection activated with "autocommit" will be by default non-transactional, with other words db connection will auto-commit changes.
     * This means that for every database call the changes will be reflected in the database right away without the possibility of reverting changes.
     * If you are performing some special actions for which you need to revert the changes you should use "transactional" database connection like so...
     */
    val exception0 = assertThrows<ConnectionException> {
        /**
         * If sistem catch any exception while in transaction mode it will revert all changes by itself.
         */
        service.transaction {
            it.row.insert(row = Parent(value = "parent1")) // We insert one row to the database.
            throw Exception("Ops something went wrong!!!")  // But unfortunately somewhere inside our code the exception occurs.
        }
    }
    assertEquals(exception0.message, "Transaction was interrupted by unhandled exception, executing rollback")

    /**
     * Because in the previous transaction exception occurred the changes are reverted inside database and our table should be still empty!
     */
    service.autocommit {
        assertTrue(it.table.select<Parent>().isEmpty())
    }

    /**
     * Transactional database connections are like git development branches. When transactional connection opens is like creating development branch
     * from the master branch and while you are creating SQL queries inside that transactional connection you are basically pushing
     * commits to your own development branch. After transaction successfully closes the development branch is then merged to master branch and deleted.
     *
     * In transactional connection you can create multiple save-points and revert commits with 'rollback' command onto specified save-point.
     * You can also 'rollback' all the changes. 'rollback' command is the same as 'reverting' commits in git. But do not forget that
     * at the end of transactional connection your development branch will be merged to master no mater what. If you revert all the changes
     * the database will stay the same as before.
     *
     * Note that this analogy with git is for the beginners in reality things are much more complicated.
     */
    service.transaction {
        /**
         * Commits:
         *  - parent1
         */
        it.row.insert(row = Parent(value = "parent1"))

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
        it.row.insert(row = Parent(value = "parent2"))

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
        it.row.insert(row = Parent(value = "parent3"))

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
        assertEquals(1, it.table.select<Parent>().size)

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
     * If your application deals with multiple database connection running at the same time querying the same database in parallel,
     * you would want to set the isolation level for your transactions. Isolation defines how or when the changes made by one operation become visible to others.
     * > https://en.wikipedia.org/wiki/Isolation_(database_systems)
     * If you need to specificaly define isolation level you can do it like so...
     */
    service.transaction(isolation = Isolation.READ_UNCOMMITTED) {
        // your operations
    }

}
