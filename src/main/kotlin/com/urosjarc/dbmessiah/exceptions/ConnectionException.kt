package com.urosjarc.dbmessiah.exceptions

/**
 * Represents a connection exception
 * signaling the problem inside database connection on which database calls
 * will execute.
 *
 * @param msg The detail message of the exception.
 * @param cause The cause of the exception.
 */
public class ConnectionException(msg: String, cause: Throwable? = null) : Error(msg, cause)
