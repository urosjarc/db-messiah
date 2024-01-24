package com.urosjarc.dbjesus.domain

import java.sql.ResultSet

typealias Decoder<T> = (rs: ResultSet, i: Int, dInfo: DecodeInfo) -> T?
