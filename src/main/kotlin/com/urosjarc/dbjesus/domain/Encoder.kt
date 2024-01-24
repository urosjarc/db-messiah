package com.urosjarc.dbjesus.domain

import java.sql.PreparedStatement

typealias Encoder = (ps: PreparedStatement, i: Int) -> Unit
