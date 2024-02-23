package com.urosjarc.dbmessiah.impl.derby

import com.urosjarc.dbmessiah.Schema
import com.urosjarc.dbmessiah.data.TypeSerializer
import com.urosjarc.dbmessiah.domain.Table

public open class DerbySchema(
    name: String,
    tables: List<Table<*>>,
    serializers: List<TypeSerializer<*>> = listOf(),
) : Schema(
    name = name,
    tables = tables,
    serializers = serializers
)
