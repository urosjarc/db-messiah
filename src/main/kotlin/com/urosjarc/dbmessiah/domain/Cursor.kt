package com.urosjarc.dbmessiah.domain

import kotlin.reflect.KProperty1

/**
 * Represents a pagination cursor.
 *
 * @param T The type of the table object.
 * @param V The type of the cursor value, which must be a comparable type.
 * @property index The current value of the cursor.
 * @property orderBy The property to order the results by.
 * @property limit The maximum number of results to return (default is 20).
 * @property order The order in which the results should be sorted (default is ASC).
 * @constructor Creates a new Cursor object.
 */
public data class Cursor<T : Any, V>(
    val index: V,
    val orderBy: KProperty1<T, V?>,
    val limit: Int = 20,
    val order: Order = Order.ASC
)
