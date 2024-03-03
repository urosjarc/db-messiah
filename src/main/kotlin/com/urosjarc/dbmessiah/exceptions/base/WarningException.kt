package com.urosjarc.dbmessiah.exceptions.base

/**
 * This class represents a user warning in the system.
 * With this exception we want to message end user that this exception
 * is fixable from the user side.
 *
 * @param msg The message associated with the warning.
 * @param cause The cause of the warning, if any.
 */
public open class WarningException(msg: String, cause: Throwable?) : Throwable(message = "USER WARNING: $msg", cause = cause)
