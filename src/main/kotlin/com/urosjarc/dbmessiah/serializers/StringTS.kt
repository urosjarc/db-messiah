package com.urosjarc.dbmessiah.serializers

import com.urosjarc.dbmessiah.data.TypeSerializer
import java.sql.JDBCType

public object StringTS {
    public fun string(size: Int): TypeSerializer<String> = TypeSerializer(
        kclass = String::class,
        dbType = "VARCHAR($size)",
        jdbcType = JDBCType.VARCHAR,
        decoder = { rs, i, _-> rs.getString(i) },
        encoder = { ps, i, x -> ps.setString(i, x) })

    public val all: List<TypeSerializer<String>> = listOf(
        string(100)
    )
}
