package com.urosjarc.dbmessiah.serializers

import com.urosjarc.dbmessiah.data.TypeSerializer
import java.sql.JDBCType

/**
 * Class provides type serializers for various non-floating point number types.
 */
public object NumberTS {
    public val byte: TypeSerializer<Byte> = TypeSerializer(
        kclass = Byte::class,
        dbType = "TINYINT",
        jdbcType = JDBCType.TINYINT,
        decoder = { rs, i, _ -> rs.getByte(i) },
        encoder = { ps, i, x -> ps.setByte(i, x) })

    public val short: TypeSerializer<Short> = TypeSerializer(
        kclass = Short::class,
        dbType = "SMALLINT",
        jdbcType = JDBCType.SMALLINT,
        decoder = { rs, i, _ -> rs.getShort(i) },
        encoder = { ps, i, x -> ps.setShort(i, x) })

    public val int: TypeSerializer<Int> = TypeSerializer(
        kclass = Int::class,
        dbType = "INTEGER",
        jdbcType = JDBCType.INTEGER,
        decoder = { rs, i, _ -> rs.getInt(i) },
        encoder = { ps, i, x -> ps.setInt(i, x) })

    public val long: TypeSerializer<Long> = TypeSerializer(
        kclass = Long::class,
        dbType = "BIGINT",
        jdbcType = JDBCType.BIGINT,
        decoder = { rs, i, _ -> rs.getLong(i) },
        encoder = { ps, i, x -> ps.setLong(i, x) })

    public val all: List<TypeSerializer<out Any>> = listOf(
        byte,
        short,
        int,
        long
    )
}
