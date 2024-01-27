package com.urosjarc.dbmessiah.domain.columns

import com.urosjarc.dbmessiah.domain.serialization.Decoder
import com.urosjarc.dbmessiah.domain.serialization.Encoder
import java.sql.JDBCType
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

open class OtherColumn(
    val unique: Boolean,
    kprop: KProperty1<*, *>,
    dbType: String,
    jdbcType: JDBCType,
    encoder: Encoder<*>,
    decoder: Decoder<*>
): Column(
    kprop = kprop,
    dbType = dbType,
    jdbcType = jdbcType,
    encoder = encoder,
    decoder = decoder
) {
    val notNull: Boolean get() = !this.kprop.returnType.isMarkedNullable
    override fun toString(): String {
        return "COL(name=$name, kclass=$kclass, dbType=$dbType, jdbcType=$jdbcType unique=$unique, notNull=$notNull)"
    }
}
