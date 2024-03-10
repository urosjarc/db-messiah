import com.urosjarc.dbmessiah.domain.Table
import com.urosjarc.dbmessiah.impl.mssql.MssqlSchema
import com.urosjarc.dbmessiah.impl.mssql.MssqlSerializer
import com.urosjarc.dbmessiah.impl.mssql.MssqlService
import com.urosjarc.dbmessiah.serializers.AllTS
import java.util.*
import kotlin.test.assertEquals

/**
 * It's not best practice to have any logic inside database since that logic belongs to our core logic (use cases).
 * > https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html
 * Type system inside Kotlin is so much superior to any logic writen inside procedure,
 * and the ease of maintaining that logic and understanding it joust can't compare to kotlin syntax.
 * In case that your database administrator does not know how to code any programming language other than SQL
 * and is pushing you to write application business rules (use cases) to the procedures...
 * I would then advise you to leave that company as soon as possible and never return back.
 * I have added support for the procedures joust because some databases that you got access to have already defined
 * some procedures at the first place and you can't help to use those procedures. It's ok to have
 * small number of procedures inside you project but please of the love of Clean Architecture and Uncle Bob,
 * do write your core logic with proper programming language.
 */

/**
 * Define your domain classes (tables)...
 */
data class Parent5(
    var pk: Int? = null,
    var value: String
)

/**
 * Define Procedures with arguments as inputs...
 */
data class Procedure50(val value: Int)
data class Procedure51(val value: Int)


/**
 * Define your database schema with registered procedures...
 * In this demonstration I will be using MS SQL server since sqlite does not have procedures
 * and postgresql support only functions.
 */
val schema5 = MssqlSchema(
    name = "schema5",
    tables = listOf(
        Table(Parent5::pk),
    ),
    procedures = listOf( // Register procedures here...
        Procedure50::class,
        Procedure51::class
    )
)

val service5 = MssqlService(
    ser = MssqlSerializer(schemas = listOf(schema5), globalSerializers = AllTS.basic),
    config = Properties().apply {
        this["jdbcUrl"] = "jdbc:sqlserver://localhost:1433;encrypt=false;"
        this["username"] = "sa"
        this["password"] = "Root_root1"
    }
)

fun main_005() {
    service5.autocommit {
        /**
         * Setup database
         */
        it.schema.create(schema = schema5)
        it.table.drop<Parent5>()
        it.table.create<Parent5>()

        /**
         * Create some elements
         */
        val parent50 = Parent5(value = "asdf")
        val parent51 = Parent5(value = "xxxx")
        it.row.insert(row = parent50)
        it.row.insert(row = parent51)

        /**
         * To create the procedure you have to provide the class which will hold input values and
         * callback where you provide execution body for the procedure. Database serializer
         * will create proper SQL in background. Note that some databases have no support for the procedures.
         */
        it.procedure.create<Procedure50> {
            "SELECT * FROM schema5.Parent5 where pk = @${Procedure50::value.name};"
        }

        /**
         * Call procedure by providing created object for that specific procedure
         * and output class that you want to receive back.
         */
        val parents50 = it.procedure.call(procedure = Procedure50(value = 1), Parent5::class)
        assertEquals(parents50, listOf(parent50))

        /**
         * Create procedure with multiple output
         */
        it.procedure.create<Procedure51> {
            """
            SELECT * FROM schema5.Parent5 where pk = @${Procedure51::value.name};
            SELECT * FROM schema5.Parent5;
            """
        }

        /**
         * Call procedure and get result.
         * Note that now that you want to receive multiple outputs the returning structure is matrix where each row represents
         * the results from that specific output.
         */
        val parents51: MutableList<List<Any>> = it.procedure.call(procedure = Procedure51(value = 1), Parent5::class, Parent5::class)
        assertEquals(parents51[0], listOf(parent50))
        assertEquals(parents51[1], listOf(parent50, parent51))
    }
}
