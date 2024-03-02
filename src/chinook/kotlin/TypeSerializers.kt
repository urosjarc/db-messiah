import com.urosjarc.dbmessiah.data.TypeSerializer
import kotlinx.datetime.*
import java.sql.JDBCType
import java.sql.Timestamp

/**
 * db-messiah supports all basic kotlin types, for external types like
 * kotlinx.datetime you can easily write custom serializers with no problem.
 */

/**
 * Custom serializer for LocalDate, conversion will be on [TimeZone.UTC].
 */
val localDate_type_serializer = TypeSerializer(
    kclass = LocalDate::class,
    dbType = "TIMESTAMP",
    jdbcType = JDBCType.TIMESTAMP,
    decoder = { rs, i, info -> rs.getTimestamp(i).toInstant().toKotlinInstant().toLocalDateTime(TimeZone.UTC).date },
    encoder = { ps, i, x -> ps.setTimestamp(i, Timestamp.from(x.atStartOfDayIn(timeZone = TimeZone.UTC).toJavaInstant())) }
)

/**
 * MS SQL server does not have [TIMESTAMP] type so for supporting this feature we will use
 * [DATETIME] instead.
 */
val mssql_localDate_type_serializer = TypeSerializer(
    kclass = LocalDate::class,
    dbType = "DATETIME",
    jdbcType = JDBCType.TIMESTAMP,
    decoder = { rs, i, info -> rs.getTimestamp(i).toInstant().toKotlinInstant().toLocalDateTime(TimeZone.UTC).date },
    encoder = { ps, i, x -> ps.setTimestamp(i, Timestamp.from(x.atStartOfDayIn(timeZone = TimeZone.UTC).toJavaInstant())) }
)

/**
 * Value classes are recognized in the system as potential foreign or primary key, but serializer will
 * still have to be defined by the user. But no problem defining them is easy.
 */
val id_serializer = TypeSerializer(
    kclass = Id::class,
    dbType = "INTEGER",
    jdbcType = JDBCType.INTEGER,
    decoder = { rs, i, info -> Id<Any>(rs.getInt(i)) },
    encoder = { ps, i, x -> ps.setInt(i, x.value) }
)
