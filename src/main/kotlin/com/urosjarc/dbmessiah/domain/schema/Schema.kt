package com.urosjarc.dbmessiah.domain.schema

import com.urosjarc.dbmessiah.domain.serialization.TypeSerializer
import com.urosjarc.dbmessiah.domain.table.Table

class Schema(
    val name: String,
    val serializers: List<TypeSerializer<out Any>> = listOf(),
    var tables: List<Table<*>>,
)
