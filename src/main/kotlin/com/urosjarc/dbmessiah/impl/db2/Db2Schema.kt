package com.urosjarc.dbmessiah.impl.db2

import com.urosjarc.dbmessiah.Schema
import com.urosjarc.dbmessiah.data.TypeSerializer
import com.urosjarc.dbmessiah.domain.Table
import kotlin.reflect.KClass

public open class Db2Schema(
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
