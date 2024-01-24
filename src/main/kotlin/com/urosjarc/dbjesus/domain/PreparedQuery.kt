package com.urosjarc.dbjesus.domain

import java.sql.PreparedStatement

class PreparedQuery(val query: Query, val ps: PreparedStatement)
