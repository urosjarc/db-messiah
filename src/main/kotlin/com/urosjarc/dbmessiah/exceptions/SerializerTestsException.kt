package com.urosjarc.dbmessiah.exceptions

import com.urosjarc.dbmessiah.exceptions.base.WarningException

/**
 * Represents an exception that occurs if any serializer test fails
 * messaging user that he created serializer with invalid configuration.
 *
 * @param msg The message describing the issue.
 * @param cause The cause of the issue, if any.
 */
internal class SerializerTestsException(msg: String, cause: Throwable? = null) : WarningException(msg = msg, cause = cause)
