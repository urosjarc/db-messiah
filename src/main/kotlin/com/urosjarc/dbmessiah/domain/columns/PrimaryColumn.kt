package com.urosjarc.dbmessiah.domain.columns

import com.urosjarc.dbmessiah.domain.serialization.Decoder
import com.urosjarc.dbmessiah.domain.serialization.Encoder
import java.sql.JDBCType
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty1

class PrimaryColumn(
    val autoIncrement: Boolean,
    override val kprop: KMutableProperty1<out Any, out Any?>,
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

    fun set(obj: Any, value: Any?){
        this.kprop as KMutableProperty1<Any, Any?>
        this.kprop.set(receiver = obj, value = value)
    }

}
