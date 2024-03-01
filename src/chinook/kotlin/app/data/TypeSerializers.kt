package app.data

import com.urosjarc.dbmessiah.data.TypeSerializer
import core.domain.Id
import kotlinx.datetime.*
import java.sql.JDBCType
import java.sql.Timestamp

val localDate_type_serializer = TypeSerializer(
    kclass = LocalDate::class,
    dbType = "TIMESTAMP",
    jdbcType = JDBCType.TIMESTAMP,
    decoder = { rs, i, info -> rs.getTimestamp(i).toInstant().toKotlinInstant().toLocalDateTime(TimeZone.UTC).date },
    encoder = { ps, i, x -> ps.setTimestamp(i, Timestamp.from(x.atStartOfDayIn(timeZone = TimeZone.UTC).toJavaInstant())) }
)

val id_serializer = TypeSerializer(
    kclass = Id::class,
    dbType = "INTEGER",
    jdbcType = JDBCType.INTEGER,
    decoder = { rs, i, info -> Id<Any>(rs.getInt(i)) },
    encoder = { ps, i, x -> ps.setInt(i, x.value) }
)
