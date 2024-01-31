package com.urosjarc.dbmessiah.exceptions.base

open class ReportIssue(msg: String, cause: Throwable? = null): Throwable(message = "PLEASE REPORT THIS ISSUE: $msg", cause=cause)
