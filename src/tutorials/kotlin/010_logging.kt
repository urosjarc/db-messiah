package logging

import com.urosjarc.dbmessiah.domain.Table
import com.urosjarc.dbmessiah.impl.sqlite.SqliteSerializer
import com.urosjarc.dbmessiah.impl.sqlite.SqliteService
import com.urosjarc.dbmessiah.serializers.AllTS
import java.util.*

/**
 * When you are using library for the first time you will be greeted with following message...
 *
 * ERROR StatusLogger Log4j2 could not find a logging implementation. Please add log4j-core to the classpath. Using SimpleLogger to log to the console...
 * SLF4J: Failed to load class "org.slf4j.impl.StaticLoggerBinder".
 * SLF4J: Defaulting to no-operation (NOP) logger implementation
 * SLF4J: See http://www.slf4j.org/codes.html#StaticLoggerBinder for further details.
 *
 * Library uses Log4j interface for logging across the sistem.
 * But since this is only an interface you will have to provide your implementation where
 * actual logging will be happening.
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
