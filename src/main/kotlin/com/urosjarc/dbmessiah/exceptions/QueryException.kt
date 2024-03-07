package com.urosjarc.dbmessiah.exceptions

import com.urosjarc.dbmessiah.exceptions.base.WarningException

/**
 * Represents an exception that occurs during query creation or execution.
 *
 * @param msg The message associated with the exception.
 * @param cause The cause of the exception, if any.
 */
public class QueryException(msg: String, cause: Throwable? = null) : WarningException(msg = msg, cause = cause)
