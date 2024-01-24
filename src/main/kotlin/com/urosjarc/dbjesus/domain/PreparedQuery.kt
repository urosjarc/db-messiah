package com.urosjarc.dbjesus.domain

import java.sql.PreparedStatement

@JvmInline
value class PreparedQuery(val preparedStatement: PreparedStatement)
