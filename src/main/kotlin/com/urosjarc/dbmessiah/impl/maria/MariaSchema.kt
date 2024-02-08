package com.urosjarc.dbmessiah.impl.maria

import com.urosjarc.dbmessiah.Schema
import com.urosjarc.dbmessiah.domain.serialization.TypeSerializer
import com.urosjarc.dbmessiah.domain.table.Table
import kotlin.reflect.KClass

open class MariaSchema(
    name: String,
    tables: List<Table<*>>,
    serializers: List<TypeSerializer<*>> = listOf(),
    procedures: List<KClass<*>> = listOf()
) : Schema(
    name = name,
    tables = tables,
    serializers = serializers,
    procedures = procedures
)
