package com.urosjarc.dbmessiah.exceptions

import com.urosjarc.dbmessiah.exceptions.base.IssueException

/**
 * Represents an exception that occurs during mapping user data to system data or vice versa.
 *
 * @param msg The message describing the issue.
 * @param cause The cause of the issue, if any.
 */
internal class MapperException(msg: String, cause: Throwable? = null) : IssueException(msg = msg, cause = cause)
