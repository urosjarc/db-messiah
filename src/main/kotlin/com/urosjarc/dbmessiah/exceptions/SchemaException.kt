package com.urosjarc.dbmessiah.exceptions

import com.urosjarc.dbmessiah.exceptions.base.WarningException

/**
 * An internal class representing an exception that occurs if user defines
 * database schema that has invalid state or incorrect configuration.
 *
 * @param msg The error message associated with the exception.
 * @param cause The cause of the exception, if any.
 *
 * @throws WarningException if a serialization error occurs.
 */
public class SchemaException(msg: String, cause: Throwable? = null) : WarningException(msg = msg, cause = cause)
