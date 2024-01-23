package com.urosjarc.diysqlservice.domain

class SqlMap(val keys: List<String>, val values: List<String>) {
    fun value(key: String): String? {
        return values.getOrNull(keys.indexOf(key))
    }

    fun merge(cb: (key: String, value: String) -> String): List<String> {
        return this.keys.mapIndexed { i, s -> cb(this.keys[i], this.values[i]) }
    }
}
