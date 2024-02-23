package com.urosjarc.dbmessiah.exceptions

/**
 * DriverException is a custom exception class that is used to handle errors that occur in the Driver class.
 *
 * @param msg The error message associated with the exception.
 * @param cause The cause of the exception.
 */
internal class DriverException(msg: String, cause: Throwable? = null) : Throwable(message = msg, cause = cause)
