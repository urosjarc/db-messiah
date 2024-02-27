package com.urosjarc.dbmessiah.exceptions

/**
 * Represents a connection exception.
 *
 * @param msg The detail message of the exception.
 * @param cause The cause of the exception.
 */
public class ConnectionException(msg: String, cause: Throwable? = null) : Error(msg, cause)
