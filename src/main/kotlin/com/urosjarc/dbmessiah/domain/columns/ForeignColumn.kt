package com.urosjarc.dbmessiah.domain.columns

import com.urosjarc.dbmessiah.domain.serialization.Decoder
import com.urosjarc.dbmessiah.domain.serialization.Encoder
import com.urosjarc.dbmessiah.domain.table.TableInfo
import java.sql.JDBCType
import kotlin.reflect.KProperty1

class ForeignColumn(
    unique: Boolean,
    kprop: KProperty1<*, *>,
    dbType: String,
    jdbcType: JDBCType,
    encoder: Encoder<*>,
    decoder: Decoder<*>,
) : OtherColumn(
    unique = unique,
    kprop = kprop,
    dbType = dbType,
    jdbcType = jdbcType,
    encoder = encoder,
    decoder = decoder
) {
    val inited get() = this::foreignTable.isInitialized

    lateinit var foreignTable: TableInfo
    override fun toString(): String {
        return "FK(name=$name, kclass=$kclass, dbType=$dbType, jdbcType=$jdbcType unique=$unique, notNull=$notNull)"
    }
}
