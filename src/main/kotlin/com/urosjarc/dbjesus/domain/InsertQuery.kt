package com.urosjarc.dbjesus.domain

class InsertQuery(
    val sql: String,
    val encoders: MutableList<Encoder<Any>>
)
