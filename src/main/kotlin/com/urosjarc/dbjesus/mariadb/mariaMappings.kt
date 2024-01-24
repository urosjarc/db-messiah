package com.urosjarc.dbjesus.mariadb

import com.urosjarc.dbjesus.SqlTypeSerializer
import com.urosjarc.dbjesus.extend.capitalized
import java.sql.JDBCType

fun stringMapping(dbType: String = "TEXT") = SqlTypeSerializer(
    kclass = String::class,
    dbType = dbType,
    jdbcType = JDBCType.VARCHAR,
    decoder = { i, rs -> rs.getString(i) },
    encoder = { it })

val intMapping = SqlTypeSerializer(
    kclass = Int::class,
    dbType = "INT",
    jdbcType = JDBCType.INTEGER,
    decoder = { i, rs -> rs.getInt(i) },
    encoder = { it.toString() })

val floatMapping = SqlTypeSerializer(
    kclass = Float::class,
    dbType = "FLOAT",
    jdbcType = JDBCType.FLOAT,
    decoder = { i, rs -> rs.getFloat(i) },
    encoder = { it.toString() })

val doubleMapping = SqlTypeSerializer(
    kclass = Double::class,
    dbType = "DOUBLE",
    jdbcType = JDBCType.DOUBLE,
    decoder = { i, rs -> rs.getDouble(i) },
    encoder = { it.toString() })

val booleanMapping = SqlTypeSerializer(
    kclass = Boolean::class,
    dbType = "BOOLEAN",
    jdbcType = JDBCType.BOOLEAN,
    decoder = { i, rs -> rs.getBoolean(i) },
    encoder = { it.toString().capitalized })

val charMapping = SqlTypeSerializer(
    kclass = Char::class,
    dbType = "CHAR",
    jdbcType = JDBCType.CHAR,
    decoder = { i, rs -> rs.getString(i).firstOrNull() },
    encoder = { it.toString().capitalized })

val baseMappings = listOf<SqlTypeSerializer<*>>(
    stringMapping(),
    intMapping,
    floatMapping,
    doubleMapping,
    booleanMapping,
    charMapping
)
