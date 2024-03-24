package com.urosjarc.dbmessiah.serializers

import com.urosjarc.dbmessiah.data.TypeSerializer
import java.sql.JDBCType
import java.sql.Timestamp
import java.time.Instant

/**
 * Represents a utility class for working with Instant values.
 */
public object InstantTS {
    public val DATETIME: TypeSerializer<Instant> = TypeSerializer(
        kclass = Instant::class,
        dbType = "DATETIME",
        jdbcType = JDBCType.TIMESTAMP,
        decoder = { rs, i, info -> rs.getTimestamp(i).toInstant() },
        encoder = { ps, i, x -> ps.setTimestamp(i, Timestamp.from(x)) }
    )
    public val TIMESTAMP: TypeSerializer<Instant> = TypeSerializer(
        kclass = Instant::class,
        dbType = "TIMESTAMP",
        jdbcType = JDBCType.TIMESTAMP,
        decoder = { rs, i, info -> rs.getTimestamp(i).toInstant() },
        encoder = { ps, i, x -> ps.setTimestamp(i, Timestamp.from(x)) }
    )
}
