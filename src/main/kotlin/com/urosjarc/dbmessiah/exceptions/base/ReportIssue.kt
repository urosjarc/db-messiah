package com.urosjarc.dbmessiah.exceptions.base


/**
 * Represents an internal exception for reporting issues.
 *
 * It is used to indicate an unexpected problem that occurred, where the cause and message should be reported.
 *
 * @param msg The message describing the issue.
 * @param cause The cause of the issue, if any.
 */
internal open class ReportIssue(msg: String, cause: Throwable? = null) : Throwable(message = "PLEASE REPORT THIS ISSUE: $msg", cause = cause)
