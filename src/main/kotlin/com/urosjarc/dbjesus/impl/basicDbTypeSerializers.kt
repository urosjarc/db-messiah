package com.urosjarc.dbjesus.impl

import com.urosjarc.dbjesus.DbTypeSerializer
import java.sql.JDBCType

fun stringSerializer(dbType: String = "TEXT") = DbTypeSerializer(
    kclass = String::class,
    dbType = dbType,
    jdbcType = JDBCType.VARCHAR,
    decoder = { rs, i, dInfo -> rs.getString(i) },
    encoder = { ps, i, x, dInfo -> ps.setString(i, x) })

val intSerializer = DbTypeSerializer(
    kclass = Int::class,
    dbType = "INT",
    jdbcType = JDBCType.INTEGER,
    decoder = { rs, i, dInfo -> rs.getInt(i) },
    encoder = { ps, i, x, eInfo -> ps.setInt(i, x) })

val floatSerializer = DbTypeSerializer(
    kclass = Float::class,
    dbType = "FLOAT",
    jdbcType = JDBCType.FLOAT,
    decoder = { rs, i, dInfo -> rs.getFloat(i) },
    encoder = { ps, i, x, eInfo -> ps.setFloat(i, x) })

val doubleSerializer = DbTypeSerializer(
    kclass = Double::class,
    dbType = "DOUBLE",
    jdbcType = JDBCType.DOUBLE,
    decoder = { rs, i, dInfo -> rs.getDouble(i) },
    encoder = { ps, i, x, eInfo -> ps.setDouble(i, x) })

val booleanSerializer = DbTypeSerializer(
    kclass = Boolean::class,
    dbType = "BOOLEAN",
    jdbcType = JDBCType.BOOLEAN,
    decoder = { rs, i, dInfo -> rs.getBoolean(i) },
    encoder = { ps, i, x, eInfo -> ps.setBoolean(i, x) })

val charSerializer = DbTypeSerializer(
    kclass = Char::class,
    dbType = "CHAR",
    jdbcType = JDBCType.CHAR,
    decoder = { rs, i, dInfo -> rs.getString(i).firstOrNull() },
    encoder = { ps, i, x, eInfo -> ps.setString(i, x.toString()) })

val basicDbTypeSerializers = listOf<DbTypeSerializer<*>>(
    stringSerializer(),
    intSerializer,
    floatSerializer,
    doubleSerializer,
    booleanSerializer,
    charSerializer
)
