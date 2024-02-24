package com.urosjarc.dbmessiah.domain

import kotlin.reflect.KProperty1

/**
 * Represents a offset pagination data for fetching items from a database table.
 *
 * @param T The generic type parameter representing the type of items being paginated.
 * @property number The page number to query.
 * @property orderBy The property to order the items by.
 * @property limit The maximum number of items to retrieve per page. Default is 20.
 * @property order The order in which items should be sorted. Default is Order.ASC.
 */
public data class Page<T : Any>(
    val number: Int,
    val orderBy: KProperty1<T, *>,
    val limit: Int = 20,
    val order: Order = Order.ASC
) {
    public val offset: Int get() = this.limit * this.number
}
