package com.urosjarc.dbmessiah.domain.serialization

import java.sql.ResultSet

internal typealias Decoder<T> = (rs: ResultSet, i: Int, dInfo: DecodeInfo) -> T?
