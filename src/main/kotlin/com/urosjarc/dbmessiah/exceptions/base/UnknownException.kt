package com.urosjarc.dbmessiah.exceptions.base

public open class UnknownException(msg: String, cause: Throwable? = null) : Throwable(message = msg, cause = cause)
