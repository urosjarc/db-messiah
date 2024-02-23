package com.urosjarc.dbmessiah.impl.postgresql

import com.urosjarc.dbmessiah.Schema
import com.urosjarc.dbmessiah.data.TypeSerializer
import com.urosjarc.dbmessiah.domain.Table
import kotlin.reflect.KClass

public open class PgSchema(
    name: String,
    tables: List<Table<*>>,
    serializers: List<TypeSerializer<*>> = listOf(),
    functions: List<KClass<*>> = listOf()
) : Schema(
    name = name,
    tables = tables,
    serializers = serializers,
    procedures = functions
)
