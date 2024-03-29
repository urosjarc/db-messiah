package com.urosjarc.dbmessiah.extend

/**
 * Returns a map of elements that are not unique in the given list.
 * The keys of the map represent the elements, and the values represent the number of occurrences of each element.
 *
 * @return a map containing elements that are not unique in the list as keys and number of occurrences as values.
 */
internal val Iterable<*>.ext_notUnique: Map<Any?, Int> get() = this.groupingBy { it }.eachCount().filter { it.value > 1 }
