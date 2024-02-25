package com.urosjarc.dbmessiah.domain

import java.sql.Connection
import java.sql.Savepoint


/**
 * The API for managing transactional connections.
 *
 * @property conn The Connection object to manage transactions on.
 */
public class Rollback(private val conn: Connection) {

    /**
     * It reverts all changes made in the current database connection.
     */
    public fun all(): Unit = this.conn.rollback()

    /**
     * Rolls back the changes made in the current database connection to the specified savepoint.
     *
     * @param point the [Savepoint] to roll back to
     */
    public fun to(point: Savepoint): Unit = this.conn.rollback(point)

    /**
     * Creates a [Savepoint] of the current transaction state.
     * That save point can be then used in [to] method.
     *
     * @return A [Savepoint] object representing the savepoint.
     */
    public fun savePoint(): Savepoint = this.conn.setSavepoint()

}
