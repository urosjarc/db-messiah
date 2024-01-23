package com.urosjarc.diysqlservice

object Env {
    val MARIA_DB_USERNAME = System.getenv("MARIA_DB_USERNAME")
    val MARIA_DB_PASSWORD = System.getenv("MARIA_DB_PASSWORD")
    val MARIA_DB_URL = System.getenv("MARIA_DB_URL")
}
