package com.urosjarc.dbmessiah.types

import com.urosjarc.dbmessiah.domain.serialization.TypeSerializer
import java.sql.JDBCType

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
