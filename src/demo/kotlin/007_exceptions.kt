import com.urosjarc.dbmessiah.domain.Table
import com.urosjarc.dbmessiah.exceptions.ConnectionException
import com.urosjarc.dbmessiah.exceptions.base.UnknownException
import com.urosjarc.dbmessiah.exceptions.base.WarningException
import com.urosjarc.dbmessiah.impl.sqlite.SqliteSerializer
import com.urosjarc.dbmessiah.serializers.AllTS
import org.junit.jupiter.api.assertThrows
import java.util.*
import kotlin.test.assertContains

/**
 * There are 3 main exceptions classes: [Issue], [Warning], [Error]...
 *
 * Warning exception: Happens when system wants to warn user of a problem that can be fixed by him...
 *      - SchemaException: It can happen on the start if user creates inconsistent or bad database structure.
 *      - SerializationException: It can happen at runtime when inconsistency is found before, during or after serialization process from db to kotlin or back.
 *
 * Issue subtypes: All exceptions such types should be reported to issue tracker since this cant be fixed by the user.
 *      - MapperException: Error that is created on Mapper that creates a maps on the start of reflection scanning.
 *      - DbValueException: Error when trying to retrieve or set value from db column, argument, etc...
 *
 * Error subtypes: All exceptions that can't be easy categorized as warning or issue, usually this exception is caused by external source...
 *      - ConnectionException: Any error created on related to db connection in the JDBC driver.
 *      - DriverException: Any error created on driver that executes db queries and sends them to JDBC driver.
 */

/**
 * System will test user schema configuration heavily at the beginning of initialization to catch any potential problems or inconsistencies created by the
 * user. After final tests are executed the sistem is checked to be valid and usualy any errors after initialization will the of form Issue or Error.
 * Warnings will usually occur if user uses API wrongly
 */

fun main_007() {
    /**
     * WarningException: Defining same table twice...
     */
    val exception0 = assertThrows<WarningException> {
        SqliteSerializer(
            tables = listOf(
                Table(Parent0::pk),
                Table(Parent0::pk),
            ),
            globalSerializers = AllTS.basic //Pass list of all basic kotlin serializers to be used by serializer (PS: Its super easy to define your own custom serializer).
        )
    }
    assertContains(exception0.stackTraceToString(), "USER WARNING: Schema 'main' has tables ['Parent0'] registered multiple times")

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
    service0.autocommit {
        val exception2 = assertThrows<WarningException> {
            it.table.create(table = String::class)
        }
        assertContains(exception2.stackTraceToString(), "USER WARNING: Could not find table info for table: 'String'")
    }

    /**
     * There are many, many, many more warning exceptions that can happen the library will be really strict on any inconsistency
     * that is found in the database schema and it will safe guard user as much as possible.
     */
    service0.autocommit {
        /**
         * System will try to log directly into the exception message all important informations so that developer
         * will never need to check logs what happened.
         */
        val exception3 = assertThrows<UnknownException> {
            it.run.query { """SELECT * FROM XXX""" }
        }
        assertContains(exception3.stackTraceToString(), "Failed to return query results from: \n\nSELECT * FROM XXX")
        assertContains(exception3.stackTraceToString(), "[SQLITE_ERROR] SQL error or missing database (no such table: XXX)")

    }
}
