package com.urosjarc.dbmessiah.domain

import kotlin.reflect.KProperty1

/**
 * Represents a page of items from database table.
 *
 * @param T The type of fetched items.
 * @property number The page number.
 * @property orderBy The table column used to order fetched items.
 * @property limit The maximum number of items per page.
 * @property offset The offset from which to start retrieving items.
 */
public data class Page<T : Any>(
    val number: Int,
    val orderBy: KProperty1<T, *>,
    val limit: Int = 20
) {
    public val offset: Int get() = this.limit * this.number
}
