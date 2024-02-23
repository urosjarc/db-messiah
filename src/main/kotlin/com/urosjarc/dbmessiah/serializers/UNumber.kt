package com.urosjarc.dbmessiah.serializers
import com.urosjarc.dbmessiah.data.TypeSerializer
import java.sql.JDBCType

public object UNumber {
    public val ubyte: TypeSerializer<UByte> = TypeSerializer(
        kclass = UByte::class,
        dbType = "TINYINT",
        jdbcType = JDBCType.TINYINT,
        decoder = { rs, i, _ -> rs.getByte(i).toUByte() },
        encoder = { ps, i, x -> ps.setByte(i, x.toByte()) })

    public val ushort: TypeSerializer<UShort> = TypeSerializer(
        kclass = UShort::class,
        dbType = "SMALLINT",
        jdbcType = JDBCType.SMALLINT,
        decoder = { rs, i, _ -> rs.getShort(i).toUShort() },
        encoder = { ps, i, x -> ps.setShort(i, x.toShort()) })

    public val uint: TypeSerializer<UInt> = TypeSerializer(
        kclass = UInt::class,
        dbType = "INTEGER",
        jdbcType = JDBCType.INTEGER,
        decoder = { rs, i, _ -> rs.getInt(i).toUInt() },
        encoder = { ps, i, x -> ps.setInt(i, x.toInt()) })

    public val ulong: TypeSerializer<ULong> = TypeSerializer(
        kclass = ULong::class,
        dbType = "BIGINT",
        jdbcType = JDBCType.BIGINT,
        decoder = { rs, i, _ -> rs.getLong(i).toULong() },
        encoder = { ps, i, x -> ps.setLong(i, x.toLong()) })

    public val all: List<TypeSerializer<out Any>> = listOf(
        ubyte,
        ushort,
        uint,
        ulong
    )

}
