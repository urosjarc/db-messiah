import com.urosjarc.dbmessiah.domain.Isolation
import com.urosjarc.dbmessiah.domain.Table
import com.urosjarc.dbmessiah.exceptions.ConnectionException
import com.urosjarc.dbmessiah.impl.sqlite.SqliteSerializer
import com.urosjarc.dbmessiah.impl.sqlite.SqliteService
import com.urosjarc.dbmessiah.serializers.AllTS
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
    /**
     * If you know that you will be only reading from the database you can activate "readOnly" mode.
     * This flag will activate additional optimization to underlying database driver!
     * You can only use this flag in non-transactional mode.
     */
    service4.query(readOnly = false) {
        it.table.drop(table = Parent4::class)
        it.table.create(table = Parent4::class)
    }

    /**
     * Database connection activated with "query" will be by default non-transactional, with other words db connection will auto-commit changes.
     * This means that for every database call the changes will be reflected in the database right away without the possibility of reverting changes.
     * If you are performing some special actions for which you need to revert the changes you should use "transactional" database connection like so...
     */
    try {
        service4.transaction {
            it.row.insert(row = Parent4(value = "parent1")) // We insert one row to the database.
            throw Exception("Ops something went wrong!!!")  // But unfortunately somewhere inside our code the exception occurs.
        }
    } catch (e: ConnectionException) {
        println("Ops connection interrupted by unhandled exception! BUT all changes commited to db are reverted!")
    }

    /**
     * Because in the previous transaction exception occurred the changes are reverted inside database and our table should be still empty!
     */
    service4.query {
        assertTrue(it.table.select(table = Parent4::class).isEmpty())
    }

    /**
     * If we write first transaction again the underlying implementation is following...
     */
    try {
        service4.transaction(isolation = Isolation.SERIALIZABLE) {
            try {
                it.row.insert(row = Parent4(value = "parent1")) // We insert one row to the database.
            } catch (e: Throwable) { // We catch any exception with the grandparent exception type to catch any of his child exception also.
                it.roolback.all()   // In case of any exception we roll back all the changes on the connection stack.
                throw ConnectionException("Transaction interrupted by unhandled exception!")
            }
        }
    } catch (e: ConnectionException) {
        println("Ops connection interrupted by unhandled exception! BUT all changes commited to db are reverted!")
    }

    /**
     * The table is still empty...
     */
    service4.query {
        assertTrue(it.table.select(table = Parent4::class).isEmpty())
    }

    /**
     * Transactional database connections are joust like git commits but with the difference that db commits are PUSHED automatically with every commit.
     * When transactional connection is active all database calls will perform so-called db commit and then PUSH changes (commit) right away to the database!
     * Its important to note that this is difference between git commits and db commits. When you are using git commits changes are located only
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
         *  - savepoint0
         */
        val savePoint0 = it.roolback.savePoint()

        /**
         * Commits:
         *  - parent1
         *  - savepoint0
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
        val savePoint1 = it.roolback.savePoint()

        /**
         * Commits:
         *  - parent1
         *  - savepoint0
         *  - parent2
         *  - savepoint1
         *  - parent3
         */
        it.row.insert(row = Parent4(value = "parent3"))

        /**
         * Commits:
         *  - parent1
         */
        it.roolback.to(point = savePoint0)

        /**
         * At the current point we have only parent1 commit,
         * and this state is reflected in database while transaction is running!
         * Note that db commits are sent to database right away!
         */
        assertEquals(1, it.table.select(table = Parent4::class).size)

        /**
         * If you at this point rollback changes to savepoint1
         * nothing will happen because you cant rollback changes to the future,
         * that's why you call operation rollback because you can only go to the history not future!
         */
        it.roolback.to(point = savePoint1)
        assertEquals(1, it.table.select(table = Parent4::class).size)
    }

}
