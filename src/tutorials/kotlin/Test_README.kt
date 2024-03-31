import com.urosjarc.dbmessiah.Profiler
import com.urosjarc.dbmessiah.data.TypeSerializer
import com.urosjarc.dbmessiah.domain.*
import com.urosjarc.dbmessiah.impl.sqlite.SqliteSerializer
import com.urosjarc.dbmessiah.impl.sqlite.SqliteService
import com.urosjarc.dbmessiah.serializers.BasicTS
import com.urosjarc.dbmessiah.serializers.IdTS
import com.urosjarc.dbmessiah.serializers.JavaTimeTS
import org.junit.jupiter.api.Test
import java.io.File
import java.sql.JDBCType
import java.time.Duration
import java.time.Instant
import java.util.*

// START 'Primary keys'
/** TYPE SAFE ID */

@JvmInline
value class Id<T>(val value: Int): Comparable<Id<T>> {
    /** FOR PAGINATION PURPOSES */
    override fun compareTo(other: Id<T>): Int = this.value.compareTo(other.value)
    /** FOR CORRECT VALUE REPRESENTATION */
    override fun toString(): String = this.value.toString()
}

/** TYPE SAFE UID */

@JvmInline
value class UId<T>(val value: UUID = UUID.randomUUID()) : Comparable<UId<T>> {
    override fun compareTo(other: UId<T>): Int = this.value.compareTo(other.value)
    override fun toString(): String = this.value.toString()
}
// STOP
// START 'Domain'
/** PARENT */

data class Parent(
    var pk: Id<Parent>? = null, // INTEGER Auto-incremental primary key
    var value: String           // NOT NULL column
)

/** CHILD */

data class Child(
    val pk: UId<Child> = UId(), // UUID Manual primary key
    val parent_pk: Id<Parent>,  // Foreign key, NOT NULL
    val value: String?          // NULL column
)

/** UNSAFE */

data class Unsafe(
//  var pk: Int? = null,                 // Unsafe INTEGER auto-incremental primary key
    val pk: UUID = UUID.randomUUID(),    // Unsafe UUID manual primary key
    val created: Instant = Instant.now() // Support for java.time.*
)
// STOP

// START 'Database'
/** SCHEMA */

val serializer = SqliteSerializer(
    globalSerializers = BasicTS.sqlite + JavaTimeTS.sqlite + listOf(
        //         constructor    deconstructor
        IdTS.int({ Id<Any>(it) }, { it.value }), // Serializer for Id<T>
        //                 constructor
        IdTS.uuid.sqlite({ UId<Any>(it) })       // Serializer for UId<T>
    ),
    tables = listOf(
        Table(Unsafe::pk),
        Table(Parent::pk),
        Table(
            Child::pk,
            foreignKeys = listOf(
                Child::parent_pk to Parent::class
            ),
            constraints = listOf(
                Child::parent_pk to listOf(C.CASCADE_DELETE, C.CASCADE_UPDATE),
                Child::value to listOf(C.UNIQUE)
            )
        ),
    ),
)

/** CONFIG */

val config = Properties().apply {
    this["jdbcUrl"] = "jdbc:sqlite::memory:"
    this["username"] = "root"
    this["password"] = "root"
}

/** SERVICE */

val sqlite = SqliteService(
    config = config,
    ser = serializer
)
// STOP

fun main() {

    // START 'PlantUML'
    File("./build/db.pluml")
        .writeText(
            serializer.plantUML(
                withOtherColumns = false
            )
        )
    // STOP

    // START 'dbdiagram.io'
    File("./build/db.txt")
        .writeText(
            serializer.dbDiagramIO(
                withOtherColumns = false
            )
        )
    // STOP

    // START 'Operations'
    sqlite.autocommit {

        /** CREATE */

        it.table.create<Parent>()
        it.table.create<Child>()
        it.table.create<Unsafe>()

        /** INSERT */

        val parent = Parent(value = "Hello World!")
        it.row.insert(row = parent)
        assert(parent.pk != null)

        /** BATCH INSERT */

        val children = arrayOfNulls<Child>(3000).mapIndexed { i, _ ->
            Child(pk = UId(), parent_pk = parent.pk!!, value = "value_$i")
        }
        it.batch.insert(rows = children) // INSERT 1000 rows per batch

        /** SELECT */

        val parents = it.table.select<Parent>()
        assert(parents.contains(parent))

        /** SELECT PAGE */

        val page = it.table.select<Child>(
            page = Page(
                number = 2, limit = 5,
                orderBy = Child::pk,
                order = Order.DESC
            )
        )
        assert(page.size == 5)

        /** SELECT CURSOR */

        val cursor = it.table.select<Child, UId<Child>>(
            cursor = Cursor(
                row = children[3], limit = 5,
                orderBy = Child::pk, order = Order.ASC
            )
        )
        assert(cursor.size == 5)

        /** UPDATE */

        parent.value = "value_3"
        it.row.update(parent)

        /** WHERE */

        val someChildren = it.query.get(output = Child::class, input = parent) {
            """ ${it.SELECT<Child>()} WHERE ${it.column(Child::value)} = ${it.input(Parent::value)} """
        }
        assert(someChildren == listOf(children[3]))

        /** JOIN */

        val moreChildren = it.query.get(output = Child::class, input = parent) {
            """
            ${it.SELECT<Child>()}
            INNER JOIN ${it.table<Parent>()} ON ${it.column(Parent::pk)} = ${it.column(Child::parent_pk)}
            WHERE ${it.column(Child::value)} = ${it.input(Parent::value)}
        """
        }
        println(moreChildren)
        assert(moreChildren == listOf(children[3]))
    }
    // STOP

    // START 'Transactions'
    sqlite.transaction { // Any exception inside will trigger rollback ALL!
        //...
        val savePoint = it.roolback.savePoint()
        //...
        it.roolback.to(point = savePoint)
        //...
    }

    /** ISOLATION */

    sqlite.transaction(isolation = Isolation.READ_UNCOMMITTED) {
        //...
    }
    // STOP

    // START 'Serializers'
    /** SERIALIZE: Instant(TIMESTAMP) */

    val DURATION = TypeSerializer(
        kclass = Duration::class,
        dbType = "INTEGER",
        jdbcType = JDBCType.INTEGER,
        decoder = { rs, i, _ -> Duration.ofSeconds(rs.getLong(i)) },
        encoder = { ps, i, x -> ps.setLong(i, x.toSeconds()) }
    )

    /** REGISTRATION */

    SqliteSerializer(
        globalSerializers = BasicTS.sqlite + JavaTimeTS.sqlite + listOf(DURATION),
        tables = listOf(Table(Unsafe::pk))
    )
    // STOP


    // START 'Profiler'
    sqlite.autocommit {
        Profiler.active = true

        repeat(10) { _ -> it.table.select<Parent>() }
        repeat(5) { _ -> it.table.select<Child>() }
        repeat(2) { _ -> it.table.select<Unsafe>() }

        Profiler.active = false
    }

    val profilerLogs = Profiler.logs.values
        .filter { !it.sql.contains("DROP TABLE") }
        .filter { !it.sql.contains("CREATE TABLE") }
        .sortedByDescending { it.duration / it.repetitions }

    profilerLogs.forEach {
        println("\n* Query: ${it.sql}")
        println("*    type: ${it.type}")
        println("*    exec: ${it.repetitions}")
        println("*    time: ${it.duration / it.repetitions}")
    }

    /**
     * Query: SELECT * FROM "main"."Parent"
     *    type: QUERY
     *    exec: 10
     *    time: 285.528us

     * Query: SELECT * FROM "main"."Unsafe"
     *    type: QUERY
     *    exec: 2
     *    time: 65.601us

     * Query: SELECT * FROM "main"."Child"
     *    type: QUERY
     *    exec: 5
     *    time: 40.525us
     */
    // STOP
}

class Test_README {
    @Test
    fun `test README`() {
        main()
    }
}
