package com.urosjarc.dbjesus.domain

import com.urosjarc.dbjesus.DbMapper
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

class Encoders(val sqlMapper: DbMapper) {

    val encoders: MutableList<Encoder<Any>> = mutableListOf()
    fun <T, K> add(kp: KProperty1<T, K>) {
        val ser = this.sqlMapper.getDbTypeSerializer(kp.returnType.classifier as KClass<Any>)
        this.encoders.add(ser.encoder)
    }
}
