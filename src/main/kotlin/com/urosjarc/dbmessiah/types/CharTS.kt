package com.urosjarc.dbmessiah.types

import com.urosjarc.dbmessiah.domain.serialization.TypeSerializer
import java.sql.JDBCType

object CharTS {
    val Char = TypeSerializer(
        kclass = Char::class,
        dbType = "CHAR",
        jdbcType = JDBCType.CHAR,
        decoder = { rs, i, _ -> rs.getString(i).first() },
        encoder = { ps, i, x -> ps.setString(i, x.toString()) })

    val all = listOf(
        Char
    )
}
