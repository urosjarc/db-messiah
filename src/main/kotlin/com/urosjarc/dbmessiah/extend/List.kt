package com.urosjarc.dbmessiah.extend

internal val List<*>.ext_notUnique: Map<Any?, Int> get() = this.groupingBy { it }.eachCount().filter { it.value > 1 }
