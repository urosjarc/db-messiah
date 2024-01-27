package com.urosjarc.dbmessiah.domain.queries

interface Unsafe {
    val sql: String
    val values: List<QueryValue>
}
