package com.urosjarc.dbjesus.domain.serialization

import java.sql.PreparedStatement

typealias Encoder<T> = (ps: PreparedStatement, i: Int, x: T) -> Unit
