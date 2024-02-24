package com.urosjarc.dbmessiah.domain

import kotlin.reflect.KProperty1

/**
 * Represents a cursor paging data for fetching items from a database table.
 *
 * @param T The type of the object representing the table.
 * @param V The type of the property used for ordering.
 * @property value The value of the cursor.
 * @property orderBy The property used for [value] cursor.
 * @property limit The maximum number of items to fetch. Default is 20.
 * @property order The order in which items should be sorted. Default is Order.ASC.
 */
public data class Cursor<T: Any, V: Any>(
    val value: V,
    val orderBy: KProperty1<T, V>,
    val limit: Int = 20,
    val order: Order = Order.ASC
)
