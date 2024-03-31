package primary_keys

import com.urosjarc.dbmessiah.data.TypeSerializer
import com.urosjarc.dbmessiah.domain.Table
import com.urosjarc.dbmessiah.impl.postgresql.PgSchema
import com.urosjarc.dbmessiah.impl.postgresql.PgSerializer
import com.urosjarc.dbmessiah.impl.postgresql.PgService
import com.urosjarc.dbmessiah.serializers.BasicTS
import java.sql.JDBCType
import java.util.*
import kotlin.test.assertEquals

/**
 * Let's define again domain classes but now lets see what
 * options we have when defining primary keys.
 * First is the standard AUTO_INCREMENT primary key.
 * Primary key must be mutable, optional and by default null on creation.
 * When row with this kind of primary key will be INSERT, system
 * will override null value with the value that was created by the database.
 * When row is DELETED system will reset value to null value if primary key is
 * of mutable type otherwise it will not.
 */
data class AutoInc(var pk: Int? = null, val col: String) // AUTO_INC primary key

/**
 * Next is primary key that must be defined from the user side.
 * Note that value is immutable, non-optional and without default value.
 */
data class ManualInt(val pk: Int, val col: String)

/**
 * Next is primary key of type UUID which means Universally Unique IDentifier.
 * This type is very popular when storing records across many databases.
 * Let's define table with primary key that will be assigned on database side
 * in the same manner as AUTO_INCREMENT primary key we will call it AUTO_UUID.
 * Note that not all databases support AUTO_UUID! Please referee to documentation
 * for further information.
 */
data class AutoUUID(var pk: UUID? = null, val col: String) // AUTO_UUID primary key


/**
 * For those databases that does not support AUTO_UUID primary key, you can allways
 * define such key on client side like so...
 */
data class ManualUUID(val pk: UUID = UUID.randomUUID(), val col: String) // MANUAL AUTO_UUID primary key

/**
 * If you want to go next step further you would want to have proper type safety for
 * foreign keys. Let's define Id inline classes which will hold information about foreign table type.
 */
@JvmInline
value class Id<T>(val value: Int) { // Internal type MUST be immutable and non-nullable.
    /**
     * You must override toString method because
     * default toString representation of any inline type
     * is 'KClass(value)' but this is invalid format for database!
     * Database needs to receive only value part!
     * */
    override fun toString(): String = this.value.toString()
}

@JvmInline
value class UId<T>(val value: UUID = UUID.randomUUID()) { // Internal type MUST be immutable and non-nullable.
    override fun toString(): String = this.value.toString()
}

data class ParentSafe(var pk: Id<ParentSafe>? = null, val col: String) // AUTO_INC primary key with type safety.
data class ParentUUIDSafe(var pk: UId<ParentSafe>? = null, val col: String) // AUTO_UUID primary key with type safety.
data class ChildSafe(
    val pk: Id<ChildSafe>,  // MANUAL AUTO_INC primary key with type safety.
    val fk: Id<ParentSafe>  // Foreign key that references parent table.
)

data class ChildUUIDSafe(
    val pk: UId<ChildSafe>,  // MANUAL AUTO_UUID primary key with type safety.
    val fk: UId<ParentSafe>  // Foreign key that references parent table.
)

/**
 * If you want to use custom inline classes you will have to define your own serializer for that type.
 * Here is an example how you would do it for this example. More information is provided in tutorial
 * about custom serializers.
 */

val id_serializer = TypeSerializer(
    kclass = Id::class,
    dbType = "INTEGER",
    jdbcType = JDBCType.INTEGER,
    decoder = { rs, i, _ -> Id<Any>(rs.getInt(i)) },
    encoder = { ps, i, x -> ps.setInt(i, x.value) }
)

/**
 * Custom serializer for your UUID inline class is a little bit tricky because not all databases
 * support native UUID type so for those databases, UUID type must be converted to CHAR type or something similar.
 * Please refer to file com.urosjarc.dbmessiah.serializers.UUIDTS.kt
 * There are implemented all possible UUID serializer for databases that are supported by the system.
 * Let's modify postgresql serializer for our inline classes. More information is provided in tutorial
 * about custom serializers.
 */
val uuid_serializer = TypeSerializer(
    kclass = UId::class,
    dbType = "UUID",
    jdbcType = JDBCType.JAVA_OBJECT,
    decoder = { rs, i, _ -> UId<Any>(value = rs.getObject(i) as UUID) },
    encoder = { ps, i, x -> ps.setObject(i, x.value) }
)


val schema = PgSchema(
    name = "primary_keys", tables = listOf(
        //Standard tables
        Table(AutoInc::pk),
        Table(ManualInt::pk),
        Table(AutoUUID::pk),
        Table(ManualUUID::pk),

        //Type safe tables
        Table(ParentSafe::pk),
        Table(ParentUUIDSafe::pk),
        Table(ChildSafe::pk),
        Table(ChildUUIDSafe::pk),
    )
)

/**
 * Create database serializer and explain database structure...
 */
val service = PgService(ser = PgSerializer(
    schemas = listOf(schema),
    globalSerializers = BasicTS.postgresql + listOf(id_serializer, uuid_serializer), // Don't forget to register custom serializer here!
), config = Properties().apply {
    this["jdbcUrl"] = "jdbc:postgresql://localhost:5432/public"
    this["username"] = "root"
    this["password"] = "root"
})

fun primary_keys() {
    service.autocommit {
        /**
         * Setup schema
         */
        it.schema.create(schema = schema)
        it.table.dropCascade<AutoInc>()
        it.table.dropCascade<ManualInt>()
        it.table.dropCascade<AutoUUID>()
        it.table.dropCascade<ManualUUID>()
        it.table.dropCascade<ParentSafe>()
        it.table.dropCascade<ParentUUIDSafe>()
        it.table.dropCascade<ChildSafe>()
        it.table.dropCascade<ChildUUIDSafe>()

        /**
         * Create table for tables
         */
        it.table.create<AutoInc>()
        it.table.create<ManualInt>()
        it.table.create<AutoUUID>()
        it.table.create<ManualUUID>()
        it.table.create<ParentSafe>()
        it.table.create<ParentUUIDSafe>()
        it.table.create<ChildSafe>()
        it.table.create<ChildUUIDSafe>()

        /**
         * Create one row for each standard tables
         */
        it.row.insert(AutoInc(col = "auto_inc"))
        it.row.insert(ManualInt(pk = 123, col = "manual_int"))
        it.row.insert(AutoUUID(col = "auto_uuid"))
        it.row.insert(ManualUUID(col = "manual_uuid"))

        /**
         * Lets define rows for insertion
         */
        val parentSafe = ParentSafe(col = "parent_safe")
        val parentUuidSafe = ParentUUIDSafe(col = "parent_uuid_safe")

        /**
         * Insert parent rows
         */
        it.row.insert(parentSafe)
        it.row.insert(parentUuidSafe)

        /**
         * Use newly generated primary keys for children foreign keys.
         */
        val childSafe = ChildSafe(pk = Id(23), fk = parentSafe.pk!!)
        val childUuidSafe = ChildUUIDSafe(pk = UId(), fk = parentUuidSafe.pk!!)

        /**
         * Insert children rows
         */
        it.row.insert(childSafe)
        it.row.insert(childUuidSafe)

        /**
         * Check if rows were indeed inserted.
         */
        assertEquals(actual = it.table.select<ParentSafe>(), expected = listOf(parentSafe))
        assertEquals(actual = it.table.select<ParentUUIDSafe>(), expected = listOf(parentUuidSafe))
        assertEquals(actual = it.table.select<ChildSafe>(), expected = listOf(childSafe))
        assertEquals(actual = it.table.select<ChildUUIDSafe>(), expected = listOf(childUuidSafe))
    }
}
