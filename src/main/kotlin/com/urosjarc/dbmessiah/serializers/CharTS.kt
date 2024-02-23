package com.urosjarc.dbmessiah.serializers

import com.urosjarc.dbmessiah.data.TypeSerializer
import java.sql.JDBCType

/**
 * The CharTS provides type serializers for the Char types.
 */
public object CharTS {
    public val char: TypeSerializer<Char> = TypeSerializer(
        kclass = Char::class,
        dbType = "CHAR",
        jdbcType = JDBCType.CHAR,
        decoder = { rs, i, _ -> rs.getString(i).first() },
        encoder = { ps, i, x -> ps.setString(i, x.toString()) })

    public val all: List<TypeSerializer<Char>> = listOf(
        char
    )
}
