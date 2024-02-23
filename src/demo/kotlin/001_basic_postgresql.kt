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
 * 2. Create database serializer and explain database structure...
 */
val pgSerializer = PgSerializer(
    schemas = listOf(parent_schema, child_schema),
    globalSerializers = AllTS.basic
)

/**
 * 3. Define HikariCP configuration...
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
 * 4. Create database service by providing it your db serializer and db configuration...
 */
val postgresql = PgService(config = pgConfig, ser = pgSerializer)

fun main() {
    postgresql.query { // Fetch available non-transactional connection from HikariCP connection pool ...
        /**
         * 5. Create schema
         */
        it.schema.create(schema = parent_schema)
        it.schema.create(schema = child_schema)

        /**
         * 5. Create tables
         */
        it.table.create(table = PgParent::class)
        it.table.create(table = PgChild::class)

    }
}
