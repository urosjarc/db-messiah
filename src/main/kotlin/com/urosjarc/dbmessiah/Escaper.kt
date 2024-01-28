package com.urosjarc.dbmessiah

data class Escaper(private val type: Type, private val joinStr: String) {
    enum class Type {
        SINGLE_QUOTES, DOUBLE_QUOTES, SQUARE_BRACKETS, GRAVE_ACCENT
    }

    fun wrap(string: String): String = when (this.type) {
        Type.SINGLE_QUOTES -> "'$string'"
        Type.DOUBLE_QUOTES -> "\"$string\""
        Type.SQUARE_BRACKETS -> "[$string]"
        Type.GRAVE_ACCENT -> "`$string`"
    }

    fun wrapJoin(vararg string: String): String = string.joinToString(separator = this.joinStr) { this.wrap(it) }
}
