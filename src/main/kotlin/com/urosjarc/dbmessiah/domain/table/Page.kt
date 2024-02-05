package com.urosjarc.dbmessiah.domain.table

import kotlin.reflect.KProperty1

data class Page<T : Any>(
    val number: Int,
    val orderBy: KProperty1<T, *>,
    val limit: Int = 20
) {
    val offset get() = this.limit * this.number
}
