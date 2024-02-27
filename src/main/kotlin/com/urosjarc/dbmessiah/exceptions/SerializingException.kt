package com.urosjarc.dbmessiah.exceptions

import com.urosjarc.dbmessiah.exceptions.base.UserWarning

/**
 * An internal class representing an exception that occurs during serialization process.
 *
 * @param msg The error message associated with the exception.
 * @param cause The cause of the exception, if any.
 *
 * @throws UserWarning if a serialization error occurs.
 */
internal class SerializingException(msg: String, cause: Throwable? = null) : UserWarning(msg = msg, cause = cause)
