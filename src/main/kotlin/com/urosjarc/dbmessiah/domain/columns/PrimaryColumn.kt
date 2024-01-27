package com.urosjarc.dbmessiah.domain.columns

import com.urosjarc.dbmessiah.domain.serialization.Decoder
import com.urosjarc.dbmessiah.domain.serialization.Encoder
import java.sql.JDBCType
import kotlin.reflect.KProperty1

class PrimaryColumn(
    val autoIncrement: Boolean,
    kprop: KProperty1<*, *>,
    dbType: String,
    jdbcType: JDBCType,
    encoder: Encoder<*>,
    decoder: Decoder<*>,
) : Column(
    kprop = kprop,
    dbType = dbType,
    jdbcType = jdbcType,
    encoder = encoder,
    decoder = decoder
) {
    override fun toString(): String {
        return "PK(name=$name, kclass=$kclass, dbType=$dbType, jdbcType=$jdbcType, autoInc=$autoIncrement)"
    }

}
