package com.urosjarc.dbjesus.extend

val List<*>.ext_notUnique get() = this.groupingBy { it }.eachCount().filter { it.value > 1 }
