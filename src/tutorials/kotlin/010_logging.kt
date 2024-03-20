package logging

import com.urosjarc.dbmessiah.domain.Table
import com.urosjarc.dbmessiah.impl.sqlite.SqliteSerializer
import com.urosjarc.dbmessiah.impl.sqlite.SqliteService
import com.urosjarc.dbmessiah.serializers.AllTS
import java.util.*

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
    ser = SqliteSerializer(tables = listOf(Table(Parent::pk)), globalSerializers = AllTS.sqlite)
)

fun logging() {

}
