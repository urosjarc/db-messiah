package com.urosjarc.dbmessiah.exceptions

import com.urosjarc.dbmessiah.exceptions.base.ReportIssue

/**
 * Represents a custom exception used in the Tester class.
 *
 * This exception extends the ReportIssue class and is used to indicate an unexpected problem
 * that occurred during testing phase, where the cause and message should be reported.
 *
 * @param msg The message describing the issue.
 * @param cause The cause of the issue, if any.
 */
internal class TesterException(msg: String, cause: Throwable? = null) : ReportIssue(msg = msg, cause = cause)
