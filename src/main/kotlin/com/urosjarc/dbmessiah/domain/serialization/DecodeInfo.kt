package com.urosjarc.dbmessiah.domain.serialization

import com.urosjarc.dbmessiah.extend.ext_kclass
import kotlin.reflect.KClass
import kotlin.reflect.KParameter

class DecodeInfo(
    val kclass: KClass<*>,
    val kparam: KParameter,
) {
    override fun toString(): String {
        val pName = "name='${this.kparam.name}'"
        val pKclass = "kclass=KClass<${this.kparam.ext_kclass.simpleName}>"

        val kclass = "kclass=KClass<${kclass.simpleName}>"
        val kparam = "kparam=KParameter($pName, $pKclass)"

        return "DecodeInfo($kclass, $kparam)"
    }
}
