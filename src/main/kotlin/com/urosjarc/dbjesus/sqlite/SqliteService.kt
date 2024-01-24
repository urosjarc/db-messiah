package com.urosjarc.dbjesus.mariadb

import com.urosjarc.dbjesus.SqlSerializer
import com.urosjarc.dbjesus.SqlService
import com.zaxxer.hikari.HikariConfig
import kotlin.reflect.KClass

class SqliteService(
    config: HikariConfig,
    override val serializer: SqlSerializer<Int>,
    override val serializer: SqlSerializer
) : SqlService<Int>(config = config) {
    override fun createTable(kclass: KClass<Any>): Int {
        val query = serializer.createQuery(kclass = kclass)
        val pQuery = this.prepareQuery(query=query)
        return this.executeUpdate(pQuery = pQuery)
    }

    override fun <T : Any> selectTable(kclass: KClass<T>): List<T> {
        val query = serializer.selectQuery(kclass = kclass)
        val pQuery = this.prepareQuery(query = query)
        return this.executeQuery(pQuery=pQuery) {
            this.serializer.decode(kclass = kclass, resultSet = it)
        }
    }



}
