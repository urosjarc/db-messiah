package com.urosjarc.dbmessiah.exceptions

import com.urosjarc.dbmessiah.exceptions.base.UserWarning

internal class SerializerException(msg: String, cause: Throwable? = null) : UserWarning(msg = msg, cause = cause)
