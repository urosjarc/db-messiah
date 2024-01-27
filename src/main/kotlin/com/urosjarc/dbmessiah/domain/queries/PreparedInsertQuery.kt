package com.urosjarc.dbmessiah.domain.queries

import java.sql.PreparedStatement

class PreparedInsertQuery(val query: InsertQuery, val ps: PreparedStatement)
