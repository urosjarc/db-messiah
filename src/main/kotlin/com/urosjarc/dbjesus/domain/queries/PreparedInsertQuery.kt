package com.urosjarc.dbjesus.domain.queries

import com.urosjarc.dbjesus.domain.queries.InsertQuery
import java.sql.PreparedStatement

class PreparedInsertQuery(val query: InsertQuery, val ps: PreparedStatement)
