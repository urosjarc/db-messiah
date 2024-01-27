package com.urosjarc.dbmessiah.domain.queries

import java.sql.PreparedStatement

class PreparedQuery(val query: Query, val ps: PreparedStatement)
