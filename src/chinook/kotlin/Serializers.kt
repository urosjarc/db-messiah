import com.urosjarc.dbmessiah.impl.db2.Db2Serializer
import com.urosjarc.dbmessiah.impl.derby.DerbySerializer
import com.urosjarc.dbmessiah.impl.h2.H2Serializer
import com.urosjarc.dbmessiah.impl.maria.MariaSerializer
import com.urosjarc.dbmessiah.impl.mssql.MssqlSerializer
import com.urosjarc.dbmessiah.impl.mysql.MysqlSerializer
import com.urosjarc.dbmessiah.impl.oracle.OracleSerializer
import com.urosjarc.dbmessiah.impl.postgresql.PgSerializer
import com.urosjarc.dbmessiah.impl.sqlite.SqliteSerializer
import com.urosjarc.dbmessiah.serializers.BasicTS
import com.urosjarc.dbmessiah.serializers.IdTS
import com.urosjarc.dbmessiah.serializers.JavaTimeTS

/**
 * Prepare all database serializers.
 */
val db2_serializer = Db2Serializer(
    schemas = listOf(db2_people_schema, db2_music_schema, db2_billing_schema),
    globalSerializers = BasicTS.db2 + JavaTimeTS.db2 + listOf(IdTS.uuid.db2 { Id<Any>(it) })
)
val derby_serializer = DerbySerializer(
    schemas = listOf(derby_people_schema, derby_music_schema, derby_billing_schema),
    globalSerializers = BasicTS.derby + JavaTimeTS.derby + listOf(IdTS.uuid.derby { Id<Any>(it) })
)
val h2_serializer = H2Serializer(
    schemas = listOf(h2_people_schema, h2_music_schema, h2_billing_schema),
    globalSerializers = BasicTS.h2 + JavaTimeTS.h2 + listOf(IdTS.uuid.h2({ Id<Any>(it) }, { it.value }))
)
val maria_serializer = MariaSerializer(
    schemas = listOf(maria_people_schema, maria_music_schema, maria_billing_schema),
    globalSerializers = BasicTS.maria + JavaTimeTS.maria + listOf(IdTS.uuid.maria({ Id<Any>(it) }, { it.value }))
)
val mssql_serializer = MssqlSerializer(
    schemas = listOf(mssql_people_schema, mssql_music_schema, mssql_billing_schema),
    globalSerializers = BasicTS.mssql + JavaTimeTS.mssql + listOf(IdTS.uuid.mssql { Id<Any>(it) })
)
val mysql_serializer = MysqlSerializer(
    schemas = listOf(mysql_people_schema, mysql_music_schema, mysql_billing_schema),
    globalSerializers = BasicTS.mysql + JavaTimeTS.mysql + listOf(IdTS.uuid.mysql { Id<Any>(it) })
)
val oracle_serializer = OracleSerializer(
    schemas = listOf(oracle_system_schema),
    globalSerializers = BasicTS.oracle + JavaTimeTS.oracle + listOf(IdTS.uuid.oracle { Id<Any>(it) })
)

val postgresql_serializer = PgSerializer(
    schemas = listOf(pg_people_schema, pg_music_schema, pg_billing_schema),
    globalSerializers = BasicTS.postgresql + JavaTimeTS.postgresql + listOf(IdTS.uuid.sqlite { Id<Any>(it) })
)

/**
 * Sqlite does not have schema so we will flaten matrix of tables in 1D array.
 */
val sqlite_serializer = SqliteSerializer(
    tables = listOf(music_tables, billing_tables, people_tables).flatten(),
    globalSerializers = BasicTS.basic + JavaTimeTS.sqlite + listOf(IdTS.uuid.sqlite { Id<Any>(it) })
)
