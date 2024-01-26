package com.urosjarc.dbjesus.domain.schema

import com.urosjarc.dbjesus.domain.serialization.TypeSerializer
import com.urosjarc.dbjesus.domain.table.Table

class Schema(
    val name: String,
    val serializers: List<TypeSerializer<Any>> = listOf(),
    val tables: List<Table<*>>,
)
