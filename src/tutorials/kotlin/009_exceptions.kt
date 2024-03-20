package exceptions

import com.urosjarc.dbmessiah.domain.Table
import com.urosjarc.dbmessiah.exceptions.DriverException
import com.urosjarc.dbmessiah.exceptions.QueryException
import com.urosjarc.dbmessiah.exceptions.base.WarningException
import com.urosjarc.dbmessiah.impl.sqlite.SqliteSerializer
import com.urosjarc.dbmessiah.impl.sqlite.SqliteService
import com.urosjarc.dbmessiah.serializers.AllTS
import org.junit.jupiter.api.assertThrows
import java.util.*
import kotlin.test.assertContains

/**
 * There are 3 main exceptions classes: issue, warning, unknown...
 *
 * Warning exception: Happens when system wants to warn user of a problem that can be fixed by him...
 *      - SerializerTestsException: It can happen on the start if user creates inconsistent or bad database structure.
 *      - QueryException: It can happen at runtime when inconsistency is found before, during or after query serialization process from db to kotlin or back.
 *
 * Issue subtypes: All exceptions such types should be reported to issue tracker since this cant be fixed by the user.
 *      - MapperException: Error created in Mapper process when mapping can't be found or performed...
 *      - DbValueException: Error when trying to retrieve or set bad value from db column, argument, etc...
 *
 * Unknown subtypes: All exceptions that can't be easy categorized as warning or issue, usually this exception is caused by external source...
 *      - ConnectionException: Any error related to db connection in the JDBC driver.
 *      - DriverException: Any error created by driver that executes db queries and sends them to JDBC driver.
 *
 * System will test user schema configuration heavily at the beginning of initialization to catch any potential problems or inconsistencies created by the
 * user. After final tests are executed the sistem is considered type safe and any errors after initialization will the form of Issue or Unknown exception.
 * Let's see few examples...
 */

/**
 * First define your database...
 */
data class Parent(var pk: Int? = null, var value: String)

val service = SqliteService(
    config = Properties().apply { this["jdbcUrl"] = "jdbc:sqlite::memory:" },
    ser = SqliteSerializer(tables = listOf(Table(indexing_and_profiling.Parent::pk)), globalSerializers = AllTS.sqlite)
)

fun exceptions() {
    /**
     * WarningException: Defining same table twice...
     */
    val exception0 = assertThrows<WarningException> {
        SqliteSerializer(
            tables = listOf(Table(Parent::pk), Table(Parent::pk)),
            globalSerializers = AllTS.sqlite
        )
    }
    assertContains(exception0.stackTraceToString(), "USER WARNING: Schema 'main' has tables registered multiple times: ['Parent']")

    /**
     * WarningException: Database schema is empty...
     */
    val exception1 = assertThrows<WarningException> {
        SqliteSerializer()
    }
    assertContains(exception1.stackTraceToString(), "USER WARNING: Missing schema or it has no registered table")

    /**
     * WarningException: You want to create table for class that is not registered inside database...
     */
    service.autocommit {
        val exception2 = assertThrows<QueryException> {
            it.table.create<String>()
        }
        assertContains(exception2.stackTraceToString(), "USER WARNING: Could not find registered table: 'String'")
    }

    /**
     * There are many, many, many more warning exceptions that can happen the library will be really strict on any inconsistency
     * that is found in the database schema and it will safe guard user as much as possible.
     */
    service.autocommit {
        /**
         * System will try to log directly into the exception message all important information so that developer
         * will never need to check logs what happened.
         */
        val exception3 = assertThrows<DriverException> {
            it.query.run { """SELECT * FROM XXX""" }
        }
        assertContains(exception3.stackTraceToString(), "Failed to return query results from: \n\nSELECT * FROM XXX")
        assertContains(exception3.stackTraceToString(), "[SQLITE_ERROR] SQL error or missing database (no such table: XXX)")

    }
}
