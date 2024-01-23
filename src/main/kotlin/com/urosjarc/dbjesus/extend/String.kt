package com.urosjarc.dbjesus.extend

val String.capitalized get(): String = this.replaceFirstChar(Char::titlecase)
