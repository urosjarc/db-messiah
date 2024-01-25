package com.urosjarc.dbjesus.domain

import kotlin.reflect.KProperty1

data class Page<T : Any?>(
    val number: Int,
    val orderBy: KProperty1<*, T>,
    val limit: Int = 20,
    val sort: Sort = Sort.ASC
) {
    enum class Sort { DESC, ASC }

    val offset get() = this.limit * this.number
}
