package com.urosjarc.dbjesus.mariadb

import com.urosjarc.dbjesus.SqlService
import kotlin.reflect.KClass

class SqliteSqlService: SqlService {
    override fun <T : Any> createTable(kclass: KClass<T>): String {
        TODO("Not yet implemented")
    }

    override fun <T : Any> selectTable(cls: KClass<T>): MutableList<T> {
        TODO("Not yet implemented")
    }

    override fun <T : Any> selectTable(cls: KClass<T>, sql: String): MutableList<T> {
        TODO("Not yet implemented")
    }

    override fun insertTable(obj: Any) {
        TODO("Not yet implemented")
    }

    override fun updateTable(obj: Any) {
        TODO("Not yet implemented")
    }
}
