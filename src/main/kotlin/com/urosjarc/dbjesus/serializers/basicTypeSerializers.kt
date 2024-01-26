package com.urosjarc.dbjesus.serializers

import com.urosjarc.dbjesus.domain.serialization.TypeSerializer
import java.sql.JDBCType

fun stringSerializer(dbType: String = "TEXT") = TypeSerializer(
    kclass = String::class,
    dbType = dbType,
    jdbcType = JDBCType.VARCHAR,
    decoder = { rs, i, dInfo -> rs.getString(i) },
    encoder = { ps, i, x -> ps.setString(i, x) })

val intSerializer = TypeSerializer(
    kclass = Int::class,
    dbType = "INT",
    jdbcType = JDBCType.INTEGER,
    decoder = { rs, i, dInfo -> rs.getInt(i) },
    encoder = { ps, i, x -> ps.setInt(i, x) })

val floatSerializer = TypeSerializer(
    kclass = Float::class,
    dbType = "FLOAT",
    jdbcType = JDBCType.FLOAT,
    decoder = { rs, i, dInfo -> rs.getFloat(i) },
    encoder = { ps, i, x -> ps.setFloat(i, x) })

val doubleSerializer = TypeSerializer(
    kclass = Double::class,
    dbType = "DOUBLE",
    jdbcType = JDBCType.DOUBLE,
    decoder = { rs, i, dInfo -> rs.getDouble(i) },
    encoder = { ps, i, x -> ps.setDouble(i, x) })

val booleanSerializer = TypeSerializer(
    kclass = Boolean::class,
    dbType = "BOOLEAN",
    jdbcType = JDBCType.BOOLEAN,
    decoder = { rs, i, dInfo -> rs.getBoolean(i) },
    encoder = { ps, i, x -> ps.setBoolean(i, x) })

val charSerializer = TypeSerializer(
    kclass = Char::class,
    dbType = "CHAR",
    jdbcType = JDBCType.CHAR,
    decoder = { rs, i, dInfo -> rs.getString(i).firstOrNull() },
    encoder = { ps, i, x -> ps.setString(i, x.toString()) })

val basicDbTypeSerializers = listOf<TypeSerializer<*>>(
    stringSerializer(),
    intSerializer,
    floatSerializer,
    doubleSerializer,
    booleanSerializer,
    charSerializer
)
