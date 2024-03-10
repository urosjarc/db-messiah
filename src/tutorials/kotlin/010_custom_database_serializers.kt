import com.urosjarc.dbmessiah.data.Query
import com.urosjarc.dbmessiah.data.QueryValue
import com.urosjarc.dbmessiah.data.TableInfo
import com.urosjarc.dbmessiah.data.TypeSerializer
import com.urosjarc.dbmessiah.domain.Table
import com.urosjarc.dbmessiah.impl.sqlite.SqliteSerializer
import com.urosjarc.dbmessiah.impl.sqlite.SqliteService
import com.urosjarc.dbmessiah.serializers.AllTS
import java.sql.JDBCType
import kotlin.reflect.KClass
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * It will come a time when you will become dissatisfied with the system and will want to modify it by your own needs.
 * For example, you would want to create db indexes on table creation for all string columns...
 * System is designed in such a way that you can override any part of it and plug in your own implementation
 * instead! Off course deep level of SQL and Kotlin syntax is needed before you partake overriding any part of the sistem :)
 * It's not for beginners. If you would like some help please feel free to create new issue to ask how you can achieve your goal.
 */

/**
 * In this example we will modify the functionality of database serializer that is responsible for SQL query generator...
 * This is the only part of the system that will be problematic to many users. Serializer creates SQL queries from user inputs,
 * for example following query...
 *
 * it.row.insert(row = Parent0::class)
 *
 * will create following SQL statement...
 *
 * INSERT INTO ${T.path} (${T.sqlInsertColumns()}) VALUES (${T.sqlInsertQuestions()})
 *
 * Many developer will not like the default SQL statement that system generates, those devs can easily override the default implementation like so...
 */

open class MyOwnSqliteSerializer(
    tables: List<Table<*>> = listOf(),
    globalSerializers: List<TypeSerializer<*>> = listOf(),
    globalInputs: List<KClass<*>> = listOf(),
    globalOutputs: List<KClass<*>> = listOf(),
) : SqliteSerializer( // We will override default SQLite serializer...
    tables = tables,
    globalSerializers = globalSerializers,
    globalInputs = globalInputs,
    globalOutputs = globalOutputs
) {

    override fun <T : Any> createTable(table: KClass<T>): Query {
        /**
         * First we want the retrieve all data about the table that this class represents.
         * TableInfo object contains informations about the column primary, foreign key, table name etc...
         */
        val T: TableInfo = this.mapper.getTableInfo(kclass = table)

        val col = mutableListOf<String>() // Sql list responsible for columns strings.
        val constraints = mutableListOf<String>()

        /**
         * All informations about primary key...
         */
        val autoIncrement = if (T.primaryColumn.autoInc) " AUTOINCREMENT" else ""
        col.add("${escaped(T.primaryColumn.name)} ${T.primaryColumn.dbType} PRIMARY KEY${autoIncrement}")

        /**
         * Fill all informations about foreign keys...
         */
        T.foreignColumns.forEach {
            val notNull = if (it.notNull) " NOT NULL" else ""
            val unique = if (it.unique) " UNIQUE" else ""
            val deleteCascade = if (it.cascadeDelete) " ON DELETE CASCADE" else ""
            val updateCascade = if (it.cascadeUpdate) " ON UPDATE CASCADE" else ""
            col.add("${escaped(it.name)} ${it.dbType}$notNull$unique")
            constraints.add(
                "FOREIGN KEY (${escaped(it.name)}) REFERENCES ${escaped(it.foreignTable.name)} (${escaped(it.foreignTable.primaryColumn.name)})$updateCascade$deleteCascade"
            )
        }

        /**
         * Fill all informations about other columns...
         */
        T.otherColumns.forEach {
            val notNull = if (it.notNull) " NOT NULL" else ""
            val unique = if (it.unique) " UNIQUE" else ""
            col.add("${escaped(it.name)} ${it.dbType}$notNull$unique")
        }

        //Connect all column definitions to one string
        val columns = (col + constraints).joinToString(", ")

        //Return created query
        return Query(sql = "CREATE TABLE IF NOT EXISTS ${escaped(T)} ($columns);")
    }

    /**
     * Now lets create our own FUNKY insert function :)
     */
    override fun insertRow(row: Any, batch: Boolean): Query {
        val T = this.mapper.getTableInfo(obj = row)
        val RB = T.getInsertRowBuilder()
        val qValues = RB.queryValues(obj = row)

        /**
         * We will found query value of type varchar and inject our own message into it...
         */
        val newValues = mutableListOf<QueryValue>()
        qValues.forEach { newValues.add(if (it.jdbcType == JDBCType.VARCHAR) it.copy(value = "I HAVE THE POWER!!!") else it) }

        /**
         * Now lets return new values back...
         */
        return Query(
            sql = "INSERT INTO ${escaped(T)} (${RB.sqlColumns { escaped(it) }}) VALUES (${RB.sqlQuestions()})",
            *newValues.toTypedArray(),
        )
    }

}

/**
 * Lets define our domains
 */
data class Parent9(
    var pk: Int? = null, // Parent auto-incremental primary key (Int?, Uint?)
    var value: String
)

/**
 * Now that you override default SqliteSerializer its time to create a sqlite service with your own serializer...
 */


val service9 = SqliteService(
    config = config0,
    ser = MyOwnSqliteSerializer( // Here we are using our own implementation
        tables = listOf(Table(Parent9::pk)),
        globalSerializers = AllTS.basic
    )
)

fun main_009() {
    service9.autocommit {
        /**
         * Prepare database
         */
        it.table.create<Parent9>()
        it.table.delete<Parent9>()

        /**
         * Insert default message
         */
        it.row.insert(row = Parent9(value = "This is my default message"))

        /**
         * Lets check if our implementation of injected message is working...
         */
        val parents = it.table.select<Parent9>()
        assertEquals(parents.size, 1)
        assertEquals(parents[0].value, "I HAVE THE POWER!!!")

        /**
         * For more information please refer to default db implementations inside package com.urosjarc.dbmessiah.impl.*
         * where are located all databases that the system supports.
         */

    }
}
