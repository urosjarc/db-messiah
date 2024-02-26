import com.urosjarc.dbmessiah.domain.Table
import com.urosjarc.dbmessiah.impl.mssql.MssqlSchema
import com.urosjarc.dbmessiah.impl.mssql.MssqlSerializer
import com.urosjarc.dbmessiah.impl.mssql.MssqlService
import com.urosjarc.dbmessiah.serializers.AllTS
import java.util.*
import kotlin.test.assertEquals


/**
 * Define your domain classes (tables)...
 */
data class Parent5(
    var pk: Int? = null,
    var value: String
)

/**
 * Define Procedure
 */
data class Procedure50(val value: Int)
data class Procedure51(val value: Int)


/**
 * Define your database schemas
 */
val schema5 = MssqlSchema(
    name = "schema5",
    tables = listOf(
        Table(Parent5::pk),
    ),
    procedures = listOf(
        Procedure50::class,
        Procedure51::class
    )
)

/**
 * Create database service...
 * Note that for different databases the Serializer API will differ since there are some differences between databases.
 * The same goes for the queries! Not all database will support some query functionality... The library tries to
 * adapt to those differences as much as possible to reflect supported functionality by the specific database.
 */
val ser5 = MssqlSerializer(
    schemas = listOf(schema5),
    globalSerializers = AllTS.basic
)

val service5 = MssqlService(
    config = Properties().apply {
        this["jdbcUrl"] = "jdbc:sqlserver://localhost:1433;encrypt=false;"
        this["username"] = "sa"
        this["password"] = "Root_root1"
    }, ser = ser5
)

fun main_005() {
    service5.autocommit {
        /**
         * Setup database
         */
        try {
            it.schema.create(schema = schema5)
        } catch (e: Throwable) {
        }
        it.table.drop(table = Parent5::class)
        it.table.create(table = Parent5::class)

        /**
         * Create some elements
         */
        val parent50 = Parent5(value = "asdf")
        val parent51 = Parent5(value = "xxxx")
        it.row.insert(row = parent50)
        it.row.insert(row = parent51)

        /**
         * Create procedure with one output
         */
        it.procedure.create(procedure = Procedure50::class) {
            "SELECT * FROM schema5.Parent5 where pk = @value;"
        }

        /**
         * Call procedure and get result
         */
        val parents50 = it.procedure.call(procedure = Procedure50(value = 1), Parent5::class)
        assertEquals(parents50, listOf(parent50))

        /**
         * Create procedure with multiple output
         */
        it.procedure.create(procedure = Procedure51::class) {
            """
            SELECT * FROM schema5.Parent5 where pk = @value;
            SELECT * FROM schema5.Parent5;
            """
        }

        /**
         * Call procedure and get result
         */
        val parents51: MutableList<List<Any>> = it.procedure.call(procedure = Procedure51(value = 1), Parent5::class, Parent5::class)
        assertEquals(parents51[0], listOf(parent50))
        assertEquals(parents51[1], listOf(parent50, parent51))

    }
}
