package com.urosjarc.dbmessiah.impl.derby

import com.urosjarc.dbmessiah.Schema
import com.urosjarc.dbmessiah.domain.serialization.TypeSerializer
import com.urosjarc.dbmessiah.domain.table.Table

public open class DerbySchema(
    name: String,
    tables: List<Table<*>>,
    serializers: List<TypeSerializer<*>> = listOf(),
) : Schema(
    name = name,
    tables = tables,
    serializers = serializers
)
