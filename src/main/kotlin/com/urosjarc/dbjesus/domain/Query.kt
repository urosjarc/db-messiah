package com.urosjarc.dbjesus.domain

class Query(
    val sql: String,
    val encoders: List<Encoder<Any>> = listOf()
)
