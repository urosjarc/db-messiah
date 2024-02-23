package com.urosjarc.dbmessiah.exceptions.base

/**
 * This class represents a user warning in the system.
 *
 * @param msg The message associated with the warning.
 * @param cause The cause of the warning, if any.
 */
internal open class UserWarning(msg: String, cause: Throwable?) : Throwable(message = "USER WARNING: $msg", cause = cause)
