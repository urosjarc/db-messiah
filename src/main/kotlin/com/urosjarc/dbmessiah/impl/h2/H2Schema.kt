package com.urosjarc.dbmessiah.impl.sqlite

import com.urosjarc.dbmessiah.Schema
import com.urosjarc.dbmessiah.domain.serialization.TypeSerializer
import com.urosjarc.dbmessiah.domain.table.Table

class H2Schema(
    name: String,
    tables: List<Table<*>>,
    serializers: List<TypeSerializer<*>> = listOf(),
) : Schema(
    name = name,
    tables = tables,
    serializers = serializers
)
