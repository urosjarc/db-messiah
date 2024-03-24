package com.urosjarc.dbmessiah.serializers

import com.urosjarc.dbmessiah.data.TypeSerializer
import java.sql.JDBCType
import java.sql.Time
import java.time.LocalTime

/**
 * Represents a collection of type serializers for LocalTime values.
 */
public object LocalTimeTS {

    public val TIME: TypeSerializer<LocalTime> = TypeSerializer(
        kclass = LocalTime::class,
        dbType = "TIME",
        jdbcType = JDBCType.TIME,
        decoder = { rs, i, _ -> rs.getTime(i).toLocalTime() },
        encoder = { ps, i, x -> ps.setTime(i, Time.valueOf(x)) }
    )
    public val NUMBER5: TypeSerializer<LocalTime> = TypeSerializer(
        kclass = LocalTime::class,
        dbType = "NUMBER(5, 0)",
        jdbcType = JDBCType.NUMERIC,
        decoder = { rs, i, _ -> LocalTime.ofSecondOfDay(rs.getLong(i)) },
        encoder = { ps, i, x -> ps.setInt(i, x.toSecondOfDay()) }
    )

}
