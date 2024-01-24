package com.urosjarc.dbjesus.impl

import com.urosjarc.dbjesus.DbEngine
import com.urosjarc.dbjesus.DbSerializer
import com.urosjarc.dbjesus.DbService
import com.urosjarc.dbjesus.domain.Encoders
import com.urosjarc.dbjesus.domain.Page
import com.urosjarc.dbjesus.domain.Query
import com.zaxxer.hikari.HikariConfig
import kotlin.reflect.KClass

class DbJesusService(
    config: HikariConfig,
    override val ser: DbSerializer<Int>,
) : DbService<Int> {

    override val eng: DbEngine<Int> = DbJesusEngine(config = config)
    override fun createTable(kclass: KClass<Any>): Int {
        val query = this.ser.createQuery(kclass = kclass)
        val pQuery = this.eng.prepareQuery(query = query)
        return this.eng.executeUpdate(pQuery = pQuery)
    }

    override fun <T : Any> selectTable(kclass: KClass<T>): List<T> {
        val query = this.ser.selectAllQuery(kclass = kclass)
        val pQuery = this.eng.prepareQuery(query = query)
        return this.eng.executeQuery(pQuery = pQuery) {
            this.ser.mapper.decode(kclass = kclass, resultSet = it)
        }
    }

    override fun <T : Any> selectTablePage(kclass: KClass<T>, page: Page<T>): List<T> {
        val query = this.ser.selectPageQuery(kclass = kclass, page = page)
        val pQuery = this.eng.prepareQuery(query = query)
        return this.eng.executeQuery(pQuery = pQuery) {
            this.ser.mapper.decode(kclass = kclass, resultSet = it)
        }
    }

    override fun <T : Any> insertTable(obj: T): Int? {
        val query = this.ser.insertQuery(obj = obj)
        val pQuery = this.eng.prepareInsertQuery(query = query)
        return this.eng.executeInsert(pQuery = pQuery) { rs, i -> rs.getInt(i) }.firstOrNull()
    }

    override fun updateTable(obj: Any): Int {
        val query = this.ser.updateQuery(obj = obj)
        val pQuery = this.eng.prepareQuery(query = query)
        return this.eng.executeUpdate(pQuery = pQuery)
    }

    override fun <T : Any> query(kclass: KClass<T>, getEscapedQuery: (addEncoders: Encoders) -> Query): List<T> {
        val query = this.ser.query(getEscapedQuery = getEscapedQuery)
        val pQuery = this.eng.prepareQuery(query = query)
        return this.eng.executeQuery(pQuery = pQuery) {
            this.ser.mapper.decode(resultSet = it, kclass = kclass)
        }
    }
}
