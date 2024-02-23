package com.urosjarc.dbmessiah.data

import java.sql.PreparedStatement

internal typealias Encoder<T> = (ps: PreparedStatement, i: Int, x: T) -> Unit
