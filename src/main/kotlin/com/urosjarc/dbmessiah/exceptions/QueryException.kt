package com.urosjarc.dbmessiah.exceptions

import com.urosjarc.dbmessiah.exceptions.base.WarningException

internal class QueryException(msg: String, cause: Throwable? = null) : WarningException(msg = msg, cause = cause)
