package com.urosjarc.dbmessiah.impl.mysql

import com.urosjarc.dbmessiah.Schema
import com.urosjarc.dbmessiah.domain.serialization.TypeSerializer
import com.urosjarc.dbmessiah.domain.table.Table
import kotlin.reflect.KClass

public open class MysqlSchema(
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
