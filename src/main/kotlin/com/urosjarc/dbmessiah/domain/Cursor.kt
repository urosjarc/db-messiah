package com.urosjarc.dbmessiah.domain

import com.urosjarc.dbmessiah.exceptions.MappingException
import com.urosjarc.dbmessiah.exceptions.QueryException
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
public data class Cursor<T : Any, V : Comparable<V>>(
    val index: V,
    val orderBy: KProperty1<T, V?>,
    val limit: Int = 20,
    val order: Order = Order.ASC
) {

    /**
     * Alternative constructor which provides more type safety to pagination cursor.
     *
     * @param row The row object from which the [orderBy] value will be extracted.
     * @param orderBy The index property to [order] and [limit] the results by.
     * @param limit The maximum number of results to return (default is 20).
     * @param order The order in which the results should be sorted (default is ASC).
     */
    public constructor(
        row: T,
        orderBy: KProperty1<T, V?>,
        limit: Int = 20,
        order: Order = Order.ASC
    ) : this(
        index = orderBy.get(row)!!,
        orderBy = orderBy,
        limit = limit,
        order = order
    )
}
