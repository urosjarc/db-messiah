package com.urosjarc.dbmessiah.exceptions

import com.urosjarc.dbmessiah.exceptions.base.IssueException

/**
 * Represents an internal exception for reporting issues related to database values.
 *
 * This exception is used to indicate an unexpected problem that occurred while manipulating database values, such as setting
 * an immutable property, encountering incompatible types, or dealing with properties that do not belong to the object.
 *
 * @param msg The message describing the issue.
 * @param cause The cause of the issue, if any.
 */
public class DbValueException(msg: String, cause: Throwable? = null) : IssueException(msg = msg, cause = cause)
