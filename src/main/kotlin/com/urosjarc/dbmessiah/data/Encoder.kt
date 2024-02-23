package com.urosjarc.dbmessiah.data

import java.sql.PreparedStatement

/**
 * Represents an encoder function that encodes a value of type [T] into a [PreparedStatement].
 *
 * @param ps The [PreparedStatement] to encode the value into.
 * @param i The index of the value in the prepared statement to set the encoded value.
 * @param x The value to encode.
 */
internal typealias Encoder<T> = (ps: PreparedStatement, i: Int, x: T) -> Unit
