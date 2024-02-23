package com.urosjarc.dbmessiah.exceptions

/**
 * Represents a service exception.
 *
 * @param msg The detail message of the exception.
 * @param cause The cause of the exception.
 */
internal class ServiceException(msg: String, cause: Throwable? = null): Throwable(msg, cause)
