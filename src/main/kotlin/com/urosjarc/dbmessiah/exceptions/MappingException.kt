package com.urosjarc.dbmessiah.exceptions

import com.urosjarc.dbmessiah.exceptions.base.WarningException

/**
 * An internal class representing an exception that occurs during serialization process.
 *
 * @param msg The error message associated with the exception.
 * @param cause The cause of the exception, if any.
 *
 * @throws WarningException if a serialization error occurs.
 */
internal class MappingException(msg: String, cause: Throwable? = null) : WarningException(msg = msg, cause = cause)
