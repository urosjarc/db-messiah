package com.urosjarc.dbmessiah.serializers

import com.urosjarc.dbmessiah.data.TypeSerializer
import java.sql.JDBCType
import java.sql.SQLException
import java.util.*

/**
 * Represents a collection of type serializers for UUID in different databases.
 */
public object UUIDTS {

    public val sqlite: TypeSerializer<UUID> = TypeSerializer(
        kclass = UUID::class,
        dbType = "CHARACTER(36)",
        jdbcType = JDBCType.CHAR,
        decoder = { rs, i, _ -> UUID.fromString(rs.getString(i)) },
        encoder = { ps, i, x -> ps.setString(i, x.toString()) })

    public val postgresql: TypeSerializer<UUID> = TypeSerializer(
        kclass = UUID::class,
        dbType = "UUID",
        jdbcType = JDBCType.JAVA_OBJECT,
        decoder = { rs, i, _ -> UUID.fromString(rs.getString(i)) },
        encoder = { ps, i, x -> ps.setObject(i, x) })

    public val oracle: TypeSerializer<UUID> = TypeSerializer(
        kclass = UUID::class,
        dbType = "VARCHAR2(36)",
        jdbcType = JDBCType.VARCHAR,
        decoder = { rs, i, _ -> UUID.fromString(rs.getString(i)) },
        encoder = { ps, i, x -> ps.setString(i, x.toString()) })

    public val mysql: TypeSerializer<UUID> = TypeSerializer(
        kclass = UUID::class,
        dbType = "CHAR(36)",
        jdbcType = JDBCType.CHAR,
        decoder = { rs, i, _ -> UUID.fromString(rs.getString(i)) },
        encoder = { ps, i, x -> ps.setString(i, x.toString()) })

    public val maria: TypeSerializer<UUID> = TypeSerializer(
        kclass = UUID::class,
        dbType = "UUID",
        jdbcType = JDBCType.CHAR,
        decoder = { rs, i, _ -> UUID.fromString(rs.getString(i)) },
        encoder = { ps, i, x -> ps.setString(i, x.toString()) })

    public val mssql: TypeSerializer<UUID> = TypeSerializer(
        kclass = UUID::class,
        dbType = "UNIQUEIDENTIFIER",
        jdbcType = JDBCType.CHAR,
        decoder = { rs, i, _ -> UUID.fromString(rs.getString(i)) },
        encoder = { ps, i, x -> ps.setString(i, x.toString()) })

    public val h2: TypeSerializer<UUID> = TypeSerializer(
        kclass = UUID::class,
        dbType = "UUID",
        jdbcType = JDBCType.JAVA_OBJECT,
        decoder = { rs, i, _ -> rs.getObject(i) as UUID },
        encoder = { ps, i, x -> ps.setObject(i, x) })

    public val derby: TypeSerializer<UUID> = TypeSerializer(
        kclass = UUID::class,
        dbType = "CHAR(36)",
        jdbcType = JDBCType.CHAR,
        decoder = { rs, i, _ -> UUID.fromString(rs.getString(i)) },
        encoder = { ps, i, x -> ps.setString(i, x.toString()) })

    public val db2: TypeSerializer<UUID> = TypeSerializer(
        kclass = UUID::class,
        dbType = "CHAR(36)",
        jdbcType = JDBCType.CHAR,
        decoder = { rs, i, _ -> UUID.fromString(rs.getString(i)) },
        encoder = { ps, i, x -> ps.setString(i, x.toString()) })
}
