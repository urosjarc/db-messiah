package com.urosjarc.dbmessiah.data

import java.sql.ResultSet


/**
 * Represents a callback that decodes a value of type [T] from a [ResultSet].
 *
 * @param rs The [ResultSet] from which to decode the value.
 * @param i The index of the column in the [ResultSet].
 * @param dInfo The [DecodeInfo] containing the additional information.
 * @return The decoded value of type [T].
 */
internal typealias Decoder<T> = (rs: ResultSet, i: Int, dInfo: DecodeInfo) -> T?
