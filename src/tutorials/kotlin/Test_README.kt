import com.urosjarc.dbmessiah.Profiler
import com.urosjarc.dbmessiah.data.TypeSerializer
import com.urosjarc.dbmessiah.domain.C
import com.urosjarc.dbmessiah.domain.Isolation
import com.urosjarc.dbmessiah.domain.Table
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
value class Id<T>(val value: Int) {
    override fun toString(): String = this.value.toString()
}

/** TYPE SAFE UID */

@JvmInline
value class UId<T>(val value: UUID = UUID.randomUUID()) {
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
    globalSerializers = BasicTS.sqlite + listOf(
        //        constructor    deconstructor
        IdTS.int({ Id<Any>(it) }, { it.value }), // Serializer for Id<T>
        //                 constructor
        IdTS.uuid.sqlite({ UId<Any>(it) })       // Serializer for UId<T>
    ),
    tables = listOf(
        Table(Parent::pk),
        Table(
            Child::pk,
            foreignKeys = listOf( // Foreign keys
                Child::parent_pk to Parent::class
            ),
            constraints = listOf( // Column constraints
                Child::parent_pk to listOf(C.UNIQUE, C.CASCADE_DELETE, C.CASCADE_UPDATE)
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

        Profiler.active = true // Activate profiler

        /** CREATE */

        it.table.create<Parent>()
        it.table.create<Child>()

        /** INSERT */

        val parent = Parent(value = "Hello World!")
        it.row.insert(row = parent)
        assert(parent.pk != null)

        /** INSERT */

        val child = Child(pk = UId(), parent_pk = parent.pk!!, value = "child value")
        it.row.insert(row = child)

        /** SELECT */

        val parents = it.table.select<Parent>()
        assert(parents.contains(parent))

        /** UPDATE */

        parent.value = "child value"
        it.row.update(parent)

        /** WHERE */

        val someChildren = it.query.get(output = Child::class, input = parent) {
            """ ${it.SELECT<Child>()} WHERE ${it.column(Child::value)} = ${it.input(Parent::value)} """
        }
        assert(someChildren.size > 0)

        /** JOIN */

        val moreChildren = it.query.get(output = Child::class, input = parent) {
            """
            ${it.SELECT<Child>()}
            INNER JOIN ${it.table<Parent>()} ON ${it.column(Parent::pk)} = ${it.column(Child::parent_pk)}
            WHERE ${it.column(Parent::value)} = ${it.input(Parent::value)}
        """
        }
        assert(moreChildren.size > 0)

        Profiler.active = false // Deactivate profiler
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
    /** TOP 5 SLOWEST QUERIES */

    val top5 = Profiler.logs.values
        .filter { !it.sql.contains("DROP TABLE") }
        .filter { !it.sql.contains("CREATE TABLE") }
        .sortedByDescending { it.duration / it.repetitions }
        .subList(0, 5)

    for (ql in top5) {
        println("\nQuery: ${ql.sql}")
        println("  * Type: ${ql.type}")
        println("  * Exec: ${ql.repetitions}")
        println("  * Time: ${ql.duration / ql.repetitions}")
    }

    /**
    Query: SELECT * FROM "main"."Parent"
     * Type: QUERY
     * Exec: 1
     * Time: 318.53us

    Query: INSERT INTO "main"."Parent" ("value") VALUES (?)
     * Type: INSERT
     * Exec: 1
     * Time: 317.288us

    Query: INSERT INTO "main"."Child" ("pk", "parent_pk", "value") VALUES (?, ?, ?)
     * Type: UPDATE
     * Exec: 1
     * Time: 185.032us

    Query: UPDATE "main"."Parent" SET "value" = ? WHERE "main"."Parent"."pk" = ?
     * Type: UPDATE
     * Exec: 1
     * Time: 153.494us

    Query:  SELECT * FROM "main"."Child" WHERE "main"."Child"."value" = ?
     * Type: QUERY
     * Exec: 1
     * Time: 149.654us
     */
    // STOP
}

class Test_README {
    @Test
    fun `test README`() {
        main()
    }
}
