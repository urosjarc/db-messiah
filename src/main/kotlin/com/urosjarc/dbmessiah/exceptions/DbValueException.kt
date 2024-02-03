package com.urosjarc.dbmessiah.exceptions

import com.urosjarc.dbmessiah.exceptions.base.ReportIssue

class DbValueException(msg: String, cause: Throwable? = null): ReportIssue(msg=msg, cause=cause)
