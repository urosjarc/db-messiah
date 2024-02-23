import com.urosjarc.dbmessiah.domain.Table
import com.urosjarc.dbmessiah.impl.postgresql.PgSchema
import com.urosjarc.dbmessiah.impl.postgresql.PgSerializer
import com.urosjarc.dbmessiah.impl.postgresql.PgService
import com.urosjarc.dbmessiah.impl.sqlite.SqliteService
import com.urosjarc.dbmessiah.serializers.AllTS
import org.junit.jupiter.api.Test
import java.util.*
import kotlin.test.*


/**
 * 1. Define your domain classes (tables)...
 */
data class PgParent(
    var pk: Int? = null, // Primary keys should be mutable (since value will be set on insert action)...
    var value: String
)

data class PgChild(
    var pk: Int? = null, // Primary keys should be mutable (since value will be set on insert action)...
    val parent_pk: Int,  // Foreign key pointing to parent primary key...
    var value: String
)

/**
 * 2. Define your database schemas
 *
 * In sqlite there is no schema to be defined (sqlite does have schemas).
 * But in postgresql you have schemas so you have to define your tables to
 * appropriate schema and follow best practice for that specific database.
 */
val parent_schema = PgSchema(
    name = "parent_schema",
    tables = listOf(
        Table(PgParent::pk), //Pass primary key reference to table to register `Parent` as db table and mark `pk` as primary key.
    )
)

val child_schema = PgSchema(
    name = "child_schema", tables = listOf(
        Table(
            PgChild::pk, foreignKeys = listOf(
                PgChild::parent_pk to PgParent::class // Define mapping from foreign key to parent table.
            )
        )
    )
)

/**
 * 3. Create database serializer and explain database structure...
 */
val pgSerializer = PgSerializer(
    schemas = listOf(parent_schema, child_schema),
    globalSerializers = AllTS.basic
)

/**
 * 4. Define HikariCP configuration...
 *
 * > https://github.com/brettwooldridge/HikariCP
 * > https://github.com/brettwooldridge/HikariCP?tab=readme-ov-file#rocket-initialization
 */
val pgConfig = Properties().apply {
    this["jdbcUrl"] = "jdbc:postgresql://localhost:5432/public"
    this["username"] = "root"
    this["password"] = "root"
}

/**
 * 5. Create database service by providing it your db serializer and db configuration...
 */
val postgresql = PgService(config = pgConfig, ser = pgSerializer)

fun main_001() {
    postgresql.query {
        /**
         * 6. Create schema
         */
        it.schema.create(schema = parent_schema)
        it.schema.create(schema = child_schema)

        /**
         * 7. Create tables
         */
        it.table.create(table = PgParent::class)
        it.table.create(table = PgChild::class)

        /**
         * 8. Make some batch inserts
         *
         * Note that batch inserts are highly optimized! If you don't care
         * to retrieve primary keys then batch operations are preferred!
         * Library will group batch operations in groups of 1000 and then execute
         * each group one by one in order to not block the database process.
         */
//        it.batch.insert()

        /**
         * 8. Drop tables
         */
        it.table.drop(table = PgChild::class)
        it.table.drop(table = PgParent::class)

        /**
         * 9. Drop schema
         */
        it.schema.drop(schema = parent_schema)
        it.schema.drop(schema = child_schema)

    }
}
