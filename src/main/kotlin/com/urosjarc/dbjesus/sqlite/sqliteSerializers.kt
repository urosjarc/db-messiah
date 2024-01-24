package com.urosjarc.dbjesus.sqlite

import com.urosjarc.dbjesus.SqlTypeSerializer
import java.sql.JDBCType

fun stringSerializer(dbType: String = "TEXT") = SqlTypeSerializer(
    kclass = String::class,
    dbType = dbType,
    jdbcType = JDBCType.VARCHAR,
    decoder = { rs, i, dInfo -> rs.getString(i) },
    encoder = { ps, i, x, dInfo -> ps.setString(i, x) })

val intSerializer = SqlTypeSerializer(
    kclass = Int::class,
    dbType = "INT",
    jdbcType = JDBCType.INTEGER,
    decoder = { rs, i, dInfo -> rs.getInt(i) },
    encoder = { ps, i, x, eInfo -> ps.setInt(i, x) })

val floatSerializer = SqlTypeSerializer(
    kclass = Float::class,
    dbType = "FLOAT",
    jdbcType = JDBCType.FLOAT,
    decoder = { rs, i, dInfo -> rs.getFloat(i) },
    encoder = { ps, i, x, eInfo -> ps.setFloat(i, x) })

val doubleSerializer = SqlTypeSerializer(
    kclass = Double::class,
    dbType = "DOUBLE",
    jdbcType = JDBCType.DOUBLE,
    decoder = { rs, i, dInfo -> rs.getDouble(i) },
    encoder = { ps, i, x, eInfo -> ps.setDouble(i, x) })

val booleanSerializer = SqlTypeSerializer(
    kclass = Boolean::class,
    dbType = "BOOLEAN",
    jdbcType = JDBCType.BOOLEAN,
    decoder = { rs, i, dInfo -> rs.getBoolean(i) },
    encoder = { ps, i, x, eInfo -> ps.setBoolean(i, x) })

val charSerializer = SqlTypeSerializer(
    kclass = Char::class,
    dbType = "CHAR",
    jdbcType = JDBCType.CHAR,
    decoder = { rs, i, dInfo -> rs.getString(i).firstOrNull() },
    encoder = { ps, i, x, eInfo -> ps.setString(i, x.toString()) })

val sqliteSerializers = listOf<SqlTypeSerializer<*>>(
    stringSerializer(),
    intSerializer,
    floatSerializer,
    doubleSerializer,
    booleanSerializer,
    charSerializer
)
