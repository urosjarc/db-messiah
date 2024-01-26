package com.urosjarc.dbjesus.domain.queries

import com.urosjarc.dbjesus.domain.queries.Query
import java.sql.PreparedStatement

class PreparedQuery(val query: Query, val ps: PreparedStatement)
