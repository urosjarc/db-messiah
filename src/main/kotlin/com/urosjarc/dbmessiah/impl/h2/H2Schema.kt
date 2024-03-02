package com.urosjarc.dbmessiah.impl.h2

import com.urosjarc.dbmessiah.Schema
import com.urosjarc.dbmessiah.data.TypeSerializer
import com.urosjarc.dbmessiah.domain.Table
import kotlin.reflect.KClass

public open class H2Schema(
    name: String,
    tables: List<Table<*>>,
    serializers: List<TypeSerializer<*>> = listOf()
) : Schema(
    name = name,
    tables = tables,
    serializers = serializers
)
