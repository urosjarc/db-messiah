package com.urosjarc.dbmessiah.data

import java.sql.ResultSet

internal typealias Decoder<T> = (rs: ResultSet, i: Int, dInfo: DecodeInfo) -> T?
