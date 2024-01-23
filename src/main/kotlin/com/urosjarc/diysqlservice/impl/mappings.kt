package com.urosjarc.diysqlservice.impl

import com.urosjarc.diysqlservice.domain.SqlMapping
import com.urosjarc.diysqlservice.extend.capitalize
import java.sql.JDBCType

fun stringMapping(dbType: String = "TEXT") = SqlMapping(
    kclass = String::class,
    dbType = dbType,
    jdbcType = JDBCType.VARCHAR,
    decoding = { i, rs -> rs.getString(i) },
    encoding = { it })

val intMapping = SqlMapping(
    kclass = Int::class,
    dbType = "INT",
    jdbcType = JDBCType.INTEGER,
    decoding = { i, rs -> rs.getInt(i) },
    encoding = { it.toString() })

val floatMapping = SqlMapping(
    kclass = Float::class,
    dbType = "FLOAT",
    jdbcType = JDBCType.FLOAT,
    decoding = { i, rs -> rs.getFloat(i) },
    encoding = { it.toString() })

val doubleMapping = SqlMapping(
    kclass = Float::class,
    dbType = "DOUBLE",
    jdbcType = JDBCType.DOUBLE,
    decoding = { i, rs -> rs.getFloat(i) },
    encoding = { it.toString() })

val booleanMapping = SqlMapping(
    kclass = Boolean::class,
    dbType = "BOOLEAN",
    jdbcType = JDBCType.BOOLEAN,
    decoding = { i, rs -> rs.getBoolean(i) },
    encoding = { it.toString().capitalize() })

val charMapping = SqlMapping(
    kclass = Char::class,
    dbType = "CHAR",
    jdbcType = JDBCType.CHAR,
    decoding = { i, rs -> rs.getString(i).firstOrNull() },
    encoding = { it.toString().capitalize() })

val baseMappings = listOf<SqlMapping<*>>(
    stringMapping(),
    intMapping,
    floatMapping,
    doubleMapping,
    booleanMapping,
    charMapping
)
