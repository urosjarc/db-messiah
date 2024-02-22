package com.urosjarc.dbmessiah.exceptions

import com.urosjarc.dbmessiah.exceptions.base.ReportIssue

internal class MapperException(msg: String, cause: Throwable? = null) : ReportIssue(msg = msg, cause = cause)
