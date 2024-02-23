package com.urosjarc.dbmessiah.domain

import kotlin.reflect.KProperty1

public data class Page<T : Any>(
    val number: Int,
    val orderBy: KProperty1<T, *>,
    val limit: Int = 20
) {
    public val offset: Int get() = this.limit * this.number
}
