package com.urosjarc.dbmessiah.impl.sqlite

import com.urosjarc.dbmessiah.Schema
import com.urosjarc.dbmessiah.data.TypeSerializer
import com.urosjarc.dbmessiah.domain.Table

public class SqliteSchema(
    name: String,
    tables: List<Table<*>>,
    serializers: List<TypeSerializer<*>> = listOf(),
) : Schema(
    name = name,
    tables = tables,
    serializers = serializers
)
