package app.data

import com.urosjarc.dbmessiah.impl.postgresql.PgSchema
import com.urosjarc.dbmessiah.impl.postgresql.PgSerializer
import com.urosjarc.dbmessiah.impl.sqlite.SqliteSerializer
import com.urosjarc.dbmessiah.serializers.AllTS

val sqlite_serializer = SqliteSerializer(
    tables = listOf(
        music_schema,
        billing_schema,
        people_schema
    ).flatMap { it.second },
    globalSerializers = AllTS.basic + listOf(localDate_type_serializer, id_serializer)
)

val postgresql_serializer = PgSerializer(
    schemas = listOf(
        music_schema,
        billing_schema,
        people_schema
    ).map { PgSchema(name = it.first, tables = it.second) },
    globalSerializers = AllTS.basic + listOf(localDate_type_serializer, id_serializer)
)
