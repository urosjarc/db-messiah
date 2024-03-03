package com.urosjarc.dbmessiah.exceptions.base

/**
 * UnknownException is a custom exception class that is used to handle unknown errors which
 * can't be categorized as [IssueException] or [WarningException].
 *
 * @param msg The error message associated with the exception.
 * @param cause The cause of the exception.
 */
public open class UnknownException(msg: String, cause: Throwable? = null) : Throwable(message = msg, cause = cause)
