package com.urosjarc.dbmessiah.extend

val String.capitalized get(): String = this.replaceFirstChar(Char::titlecase)
