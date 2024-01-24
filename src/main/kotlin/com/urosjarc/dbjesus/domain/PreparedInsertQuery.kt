package com.urosjarc.dbjesus.domain

import java.sql.PreparedStatement

@JvmInline
value class PreparedInsertQuery(val preparedStatement: PreparedStatement)
