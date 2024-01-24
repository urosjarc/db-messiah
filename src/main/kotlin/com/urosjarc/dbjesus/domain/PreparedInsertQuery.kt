package com.urosjarc.dbjesus.domain

import java.sql.PreparedStatement

class PreparedInsertQuery(val query: InsertQuery, val ps: PreparedStatement)
