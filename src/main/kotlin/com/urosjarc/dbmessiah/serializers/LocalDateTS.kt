package com.urosjarc.dbmessiah.serializers

import com.urosjarc.dbmessiah.data.TypeSerializer
import java.sql.Date
import java.sql.JDBCType
import java.time.LocalDate

/**
 * Represents a utility class for serializing and deserializing LocalDate objects.
 */
public object LocalDateTS {

    public val DATE: TypeSerializer<LocalDate> = TypeSerializer(
        kclass = LocalDate::class,
        dbType = "DATE",
        jdbcType = JDBCType.DATE,
        decoder = { rs, i, _ -> rs.getDate(i).toLocalDate() },
        encoder = { ps, i, x -> ps.setDate(i, Date.valueOf(x)) }
    )

}
