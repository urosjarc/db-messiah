package com.urosjarc.dbmessiah.exceptions.base

open class UserWarning(msg: String, cause: Throwable?): Throwable(message = "USER WARNING: $msg", cause=cause)
