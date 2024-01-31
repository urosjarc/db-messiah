package com.urosjarc.dbmessiah.extend

val List<*>.ext_notUnique: Map<Any?, Int> get() = this.groupingBy { it }.eachCount().filter { it.value > 1 }
