package com.urosjarc.dbmessiah.serializers

import com.urosjarc.dbmessiah.data.TypeSerializer
import java.sql.JDBCType
import java.util.*

/**
 * Collection of functions to help you create [TypeSerializer] for
 * your own custom inline id class. For example `inline class Id<T>(val value: UUID = UUID.random())`.
 */
public object IdTS {
    public inline fun <reified T : Any> int(crossinline construct: (value: Int) -> T, crossinline deconstruct: (value: T) -> Int): TypeSerializer<T> =
        TypeSerializer(kclass = T::class,
            dbType = "INTEGER",
            jdbcType = JDBCType.INTEGER,
            decoder = { rs, i, _ -> construct(rs.getInt(i)) },
            encoder = { ps, i, x -> ps.setInt(i, deconstruct(x)) })

    public object uuid {

        public inline fun <reified T : Any> sqlite(crossinline construct: (value: UUID) -> T): TypeSerializer<T> = TypeSerializer(kclass = T::class,
            dbType = "CHARACTER(36)",
            jdbcType = JDBCType.CHAR,
            decoder = { rs, i, _ -> construct(UUID.fromString(rs.getString(i))) },
            encoder = { ps, i, x -> ps.setString(i, x.toString()) })

        public inline fun <reified T : Any> postgresql(
            crossinline construct: (value: UUID) -> T, crossinline deconstruct: (value: T) -> UUID
        ): TypeSerializer<T> = TypeSerializer(kclass = T::class,
            dbType = "UUID",
            jdbcType = JDBCType.JAVA_OBJECT,
            decoder = { rs, i, _ -> construct(rs.getObject(i) as UUID) },
            encoder = { ps, i, x -> ps.setObject(i, deconstruct(x)) })

        public inline fun <reified T : Any> oracle(crossinline construct: (value: UUID) -> T): TypeSerializer<T> = TypeSerializer(kclass = T::class,
            dbType = "VARCHAR2(36)",
            jdbcType = JDBCType.VARCHAR,
            decoder = { rs, i, _ -> construct(UUID.fromString(rs.getString(i))) },
            encoder = { ps, i, x -> ps.setString(i, x.toString()) })

        public inline fun <reified T : Any> mysql(crossinline construct: (value: UUID) -> T): TypeSerializer<T> = TypeSerializer(kclass = T::class,
            dbType = "CHAR(36)",
            jdbcType = JDBCType.CHAR,
            decoder = { rs, i, _ -> construct(UUID.fromString(rs.getString(i))) },
            encoder = { ps, i, x -> ps.setString(i, x.toString()) })

        public inline fun <reified T : Any> maria(
            crossinline construct: (value: UUID) -> T, crossinline deconstruct: (value: T) -> UUID
        ): TypeSerializer<T> = TypeSerializer(kclass = T::class,
            dbType = "UUID",
            jdbcType = JDBCType.JAVA_OBJECT,
            decoder = { rs, i, _ -> construct(rs.getObject(i) as UUID) },
            encoder = { ps, i, x -> ps.setObject(i, deconstruct(x)) })

        public inline fun <reified T : Any> mssql(crossinline construct: (value: UUID) -> T): TypeSerializer<T> = TypeSerializer(kclass = T::class,
            dbType = "UNIQUEIDENTIFIER",
            jdbcType = JDBCType.CHAR,
            decoder = { rs, i, _ -> construct(UUID.fromString(rs.getString(i))) },
            encoder = { ps, i, x -> ps.setString(i, x.toString()) })

        public inline fun <reified T : Any> h2(
            crossinline construct: (value: UUID) -> T, crossinline deconstruct: (value: T) -> UUID
        ): TypeSerializer<T> = TypeSerializer(kclass = T::class,
            dbType = "UUID",
            jdbcType = JDBCType.JAVA_OBJECT,
            decoder = { rs, i, _ -> construct(rs.getObject(i) as UUID) },
            encoder = { ps, i, x -> ps.setObject(i, deconstruct(x)) })

        public inline fun <reified T : Any> derby(crossinline construct: (value: UUID) -> T): TypeSerializer<T> = TypeSerializer(kclass = T::class,
            dbType = "CHAR(36)",
            jdbcType = JDBCType.CHAR,
            decoder = { rs, i, _ -> construct(UUID.fromString(rs.getString(i))) },
            encoder = { ps, i, x -> ps.setString(i, x.toString()) })

        public inline fun <reified T : Any> db2(crossinline construct: (value: UUID) -> T): TypeSerializer<T> = TypeSerializer(kclass = T::class,
            dbType = "CHAR(36)",
            jdbcType = JDBCType.CHAR,
            decoder = { rs, i, _ -> construct(UUID.fromString(rs.getString(i))) },
            encoder = { ps, i, x -> ps.setString(i, x.toString()) })
    }
}
