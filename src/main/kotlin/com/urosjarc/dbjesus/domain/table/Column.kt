package com.urosjarc.dbjesus.domain.table

import com.urosjarc.dbjesus.domain.serialization.Decoder
import com.urosjarc.dbjesus.domain.serialization.Encoder
import java.sql.JDBCType
import kotlin.reflect.KClass

class Column(
    //Basic info
    val name: String,
    val value: Any?,
    val canBeNull: Boolean,

    //Type info
    val kclass: KClass<*>,
    val dbType: String,
    val jdbcType: JDBCType,

    // Connected to other table?
    val foreignTable: Table?,

    //Serialization info
    val encoder: Encoder<*>,
    val decoder: Decoder<*>
)
