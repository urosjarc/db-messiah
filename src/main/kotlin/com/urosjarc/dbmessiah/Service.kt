package com.urosjarc.dbmessiah

import java.util.*

/**
 * This class represents a Service that provides query and transactional operations on a database.
 *
 * @property conn The connection pool for creating query or transactional connections.
 * @property ser The serializer for serializing and deserializing data.
 * */
public abstract class Service {
    internal val conn: ConnectionPool
    internal val ser: Serializer

    /**
     * This class represents a Service that provides query and transactional operations on a database.
     *
     * @param config The properties configuration for the connection pool that,
     * is then directly passed to [HikariCP](https://github.com/brettwooldridge/HikariCP).
     * @param ser The serializer for serializing and deserializing data.
     */
    public constructor(config: Properties, ser: Serializer) {
        this.ser = ser
        this.conn = ConnectionPool(config)
    }

    /**
     * Construct a Service object with the given configuration path and serializer.
     *
     * @param configPath The path to properties configuration for the connection pool,
     * that is then directly passed to [HikariCP](https://github.com/brettwooldridge/HikariCP).
     * @param ser The serializer for serializing and deserializing data.
     */
    public constructor(configPath: String, ser: Serializer) {
        this.ser = ser
        this.conn = ConnectionPool(configPath)
    }
}
