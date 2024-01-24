package com.urosjarc.dbjesus.domain

import java.sql.PreparedStatement

typealias Encoder<T> = (ps: PreparedStatement, i: Int, x: T, eInfo: EncodeInfo) -> Unit
