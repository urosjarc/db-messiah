import com.urosjarc.dbmessiah.impl.db2.Db2Serializer
import com.urosjarc.dbmessiah.impl.derby.DerbySerializer
import com.urosjarc.dbmessiah.impl.h2.H2Serializer
import com.urosjarc.dbmessiah.impl.maria.MariaSerializer
import com.urosjarc.dbmessiah.impl.mssql.MssqlSerializer
import com.urosjarc.dbmessiah.impl.mysql.MysqlSerializer
import com.urosjarc.dbmessiah.impl.oracle.OracleSerializer
import com.urosjarc.dbmessiah.impl.postgresql.PgSerializer
import com.urosjarc.dbmessiah.impl.sqlite.SqliteSerializer
import com.urosjarc.dbmessiah.serializers.AllTS

val db2_serializer = Db2Serializer(
    schemas = listOf(db2_people_schema, db2_music_schema, db2_billing_schema),
    globalSerializers = AllTS.basic + listOf(localDate_type_serializer, id_serializer)
)
val derby_serializer = DerbySerializer(
    tables = listOf(music_tables, billing_tables, people_tables).flatten(),
    globalSerializers = AllTS.basic + listOf(localDate_type_serializer, id_serializer)
)
val h2_serializer = H2Serializer(
    tables = listOf(music_tables, billing_tables, people_tables).flatten(),
    globalSerializers = AllTS.basic + listOf(localDate_type_serializer, id_serializer)
)
val maria_serializer = MariaSerializer(
    schemas = listOf(maria_people_schema, maria_music_schema, maria_billing_schema),
    globalSerializers = AllTS.basic + listOf(localDate_type_serializer, id_serializer)
)
val mssql_serializer = MssqlSerializer(
    schemas = listOf(mssql_people_schema, mssql_music_schema, mssql_billing_schema),
    globalSerializers = AllTS.basic + listOf(localDate_type_serializer, id_serializer)
)
val mysql_serializer = MysqlSerializer(
    schemas = listOf(mysql_people_schema, mysql_music_schema, mysql_billing_schema),
    globalSerializers = AllTS.basic + listOf(localDate_type_serializer, id_serializer)
)
val oracle_serializer = OracleSerializer(
    schemas = listOf(oracle_people_schema, oracle_music_schema, oracle_billing_schema),
    globalSerializers = AllTS.basic + listOf(localDate_type_serializer, id_serializer)
)

val postgresql_serializer = PgSerializer(
    schemas = listOf(pg_people_schema, pg_music_schema, pg_billing_schema),
    globalSerializers = AllTS.basic + listOf(localDate_type_serializer, id_serializer)
)
val sqlite_serializer = SqliteSerializer(
    tables = listOf(music_tables, billing_tables, people_tables).flatten(),
    globalSerializers = AllTS.basic + listOf(localDate_type_serializer, id_serializer)
)
