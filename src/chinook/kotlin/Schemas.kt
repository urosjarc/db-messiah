import com.urosjarc.dbmessiah.domain.C
import com.urosjarc.dbmessiah.domain.Table
import com.urosjarc.dbmessiah.impl.db2.Db2Schema
import com.urosjarc.dbmessiah.impl.derby.DerbySchema
import com.urosjarc.dbmessiah.impl.h2.H2Schema
import com.urosjarc.dbmessiah.impl.maria.MariaSchema
import com.urosjarc.dbmessiah.impl.mssql.MssqlSchema
import com.urosjarc.dbmessiah.impl.mysql.MysqlSchema
import com.urosjarc.dbmessiah.impl.oracle.OracleSchema
import com.urosjarc.dbmessiah.impl.postgresql.PgSchema
import domain.*
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.javaField

/**
 * With a little of bit of reflection we can easily extract the foreign keys information
 * of a table that owns given primary key property. With this function we can automatically
 * build our final tables in [createTable] without any mistakes or friction.
 * To use this function is preferred when you have huge amount of tables and foreign keys.
 * This function is not included to main library because library does not want to force
 * user to use specific class with the specific name. User can define his own [Id] class
 * and define how extraction of foreign keys will happened.
 *
 * And as always, before use this or any other function understand what it does and don't trust
 * any foreign code.
 *
 * @param primaryKey the primary key property.
 * @return a mutable list of pairs, where each pair contains a foreign key property and its class.
 * @*/
fun <T : Any> extractForeignKeys(primaryKey: KProperty1<T, *>): MutableList<Pair<KProperty1<T, *>, KClass<*>>> {
    //First extract the owner of the primary key.
    val owner = primaryKey.javaField?.declaringClass?.kotlin ?: throw Exception("Could not get owner of $primaryKey")

    //Define foreign key map where we will fill all foreign keys.
    val fkMap: MutableList<Pair<KProperty1<T, *>, KClass<*>>> = mutableListOf()

    //Get all properties of the owner class.
    val kprops = owner.memberProperties.filter { it.javaField != null }

    //Scan all properties
    kprops.forEach {

        //If property type is of type Id and is not primary key then it must be foreign key
        if (it != primaryKey && it.returnType.classifier == Id::class) {

            //Extract T from Id<T> as KClass
            val foreignKClass = it.returnType.arguments.first().type?.classifier

            //Fill foreign key map
            fkMap.add(Pair(it, foreignKClass) as Pair<KProperty1<T, *>, KClass<*>>)
        }
    }
    return fkMap
}

/**
 * With [extractForeignKeys] function
 * Creates a new table with the given primary key.
 * To all foreign keys we will attach [C.CASCADE_DELETE] to ease
 * managing database data.
 *
 * @param primaryKey the primary key property of the table's entity.
 * @return the newly created [Table] that can be used in database schemas.
 */
fun <T : Any> createTable(primaryKey: KProperty1<T, *>): Table<T> {
    //Gather all foreign keys
    val foreignKeys = extractForeignKeys(primaryKey = primaryKey)
    return Table(
        primaryKey = primaryKey,
        foreignKeys = foreignKeys,
        constraints = foreignKeys.map { it.first to listOf(C.CASCADE_DELETE) })
}

/**
 * Lets create schema tables...
 */

val music_tables = listOf(
    createTable(Artist::id),
    createTable(Album::id),
    createTable(Track::id),
    createTable(MediaType::id),
    createTable(Playlist::id),
    createTable(PlaylistTrack::id),
    createTable(Genre::id),
)


val billing_tables = listOf(
    createTable(Invoice::id),
    createTable(InvoiceLine::id),
)

val people_tables = listOf(
    createTable(Employee::id),
    createTable(Customer::id),
)

/**
 * Lets prepare schemas for all databases.
 */

//H2
val h2_music_schema = H2Schema(name = "music", tables = music_tables)
val h2_billing_schema = H2Schema(name = "billing", tables = billing_tables)
val h2_people_schema = H2Schema(name = "people", tables = people_tables)

//Db2
val db2_music_schema = Db2Schema(name = "music", tables = music_tables)
val db2_billing_schema = Db2Schema(name = "billing", tables = billing_tables)
val db2_people_schema = Db2Schema(name = "people", tables = people_tables)

//Derby
val derby_music_schema = DerbySchema(name = "music", tables = music_tables)
val derby_billing_schema = DerbySchema(name = "billing", tables = billing_tables)
val derby_people_schema = DerbySchema(name = "people", tables = people_tables)

//Maria
val maria_music_schema = MariaSchema(name = "music", tables = music_tables)
val maria_billing_schema = MariaSchema(name = "billing", tables = billing_tables)
val maria_people_schema = MariaSchema(name = "people", tables = people_tables)

//Mssql
val mssql_music_schema = MssqlSchema(name = "music", tables = music_tables)
val mssql_billing_schema = MssqlSchema(name = "billing", tables = billing_tables)
val mssql_people_schema = MssqlSchema(name = "people", tables = people_tables)

//Mysql
val mysql_music_schema = MysqlSchema(name = "music", tables = music_tables)
val mysql_billing_schema = MysqlSchema(name = "billing", tables = billing_tables)
val mysql_people_schema = MysqlSchema(name = "people", tables = people_tables)

/**
 * For the oracle the schema creation is not so trivial, so we will not bother
 * with schema creation, and we will use default schema.
 */
//Oracle
val oracle_system_schema = OracleSchema(name = "SYSTEM", tables = music_tables + billing_tables + people_tables)

//Postgresql
val pg_music_schema = PgSchema(name = "music", tables = music_tables)
val pg_billing_schema = PgSchema(name = "billing", tables = billing_tables)
val pg_people_schema = PgSchema(name = "people", tables = people_tables)
