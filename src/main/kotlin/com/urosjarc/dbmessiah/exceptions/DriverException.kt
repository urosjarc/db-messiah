package com.urosjarc.dbmessiah.exceptions

import com.urosjarc.dbmessiah.exceptions.base.UnknownException

/**
 * DriverException is a custom exception class that is used to handle errors that occur in the database driver.
 *
 * @param msg The error message associated with the exception.
 * @param cause The cause of the exception.
 */
public class DriverException(msg: String, cause: Throwable? = null) : UnknownException(msg = msg, cause = cause)
