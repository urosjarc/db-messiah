package com.urosjarc.dbmessiah.exceptions

import com.urosjarc.dbmessiah.exceptions.base.UnknownException

/**
 * Represents a connection exception
 * signaling the problem inside database connection on which database calls
 * will execute.
 *
 * @param msg The detail message of the exception.
 * @param cause The cause of the exception.
 */
public class ConnectionException(msg: String, cause: Throwable? = null) : UnknownException(msg, cause)
