package com.urosjarc.dbjesus.domain

import com.urosjarc.dbjesus.SqlTypeSerializer
import com.urosjarc.dbjesus.extend.kclass
import kotlin.reflect.KProperty1

class ObjProperty(
    val name: String,
    val value: Any?,
    val property: KProperty1<Any, *>,
    val serializer: SqlTypeSerializer<Any>,
) {
    val encoder get() = this.serializer.encoder
    val kclass get() = this.property.kclass
}
