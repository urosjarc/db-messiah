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

fun <T : Any> extractForeignKeys(primaryKey: KProperty1<T, *>): MutableList<Pair<KProperty1<T, *>, KClass<*>>> {
    val owner = primaryKey.javaField?.declaringClass?.kotlin ?: throw Exception("Could not get owner of $primaryKey")
    val fkMap: MutableList<Pair<KProperty1<T, *>, KClass<*>>> = mutableListOf()
    val kprops = owner.memberProperties.filter { it.javaField != null }
    kprops.forEach {
        if (it != primaryKey && it.returnType.classifier == Id::class) {
            val foreignKClass = it.returnType.arguments.first().type?.classifier
            fkMap.add(Pair(it, foreignKClass) as Pair<KProperty1<T, *>, KClass<*>>)
        }
    }
    return fkMap
}

fun <T : Any> createTable(primaryKey: KProperty1<T, *>): Table<T> {
    val foreignKeys = extractForeignKeys(primaryKey = primaryKey)
    return Table(primaryKey = primaryKey, foreignKeys = foreignKeys, constraints = foreignKeys.map { it.first to listOf(C.CASCADE_DELETE) })
}

inline fun <reified T> name(): String = "\"${T::class.simpleName.toString()}\""
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

//Oracle
val oracle_system_schema = OracleSchema(name = "SYSTEM", tables = music_tables + billing_tables + people_tables)

//Postgresql
val pg_music_schema = PgSchema(name = "music", tables = music_tables)
val pg_billing_schema = PgSchema(name = "billing", tables = billing_tables)
val pg_people_schema = PgSchema(name = "people", tables = people_tables)
