package com.urosjarc.dbmessiah.exceptions.base

internal open class ReportIssue(msg: String, cause: Throwable? = null): Throwable(message = "PLEASE REPORT THIS ISSUE: $msg", cause=cause)
