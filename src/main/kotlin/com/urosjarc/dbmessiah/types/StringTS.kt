package com.urosjarc.dbmessiah.types

import com.urosjarc.dbmessiah.domain.serialization.TypeSerializer
import java.sql.JDBCType

object StringTS {
    fun String(size: Int) = TypeSerializer(
        kclass = String::class,
        dbType = "VARCHAR($size)",
        jdbcType = JDBCType.VARCHAR,
        decoder = { rs, i, _-> rs.getString(i) },
        encoder = { ps, i, x -> ps.setString(i, x) })

    val all = listOf(
        String(100)
    )
}
