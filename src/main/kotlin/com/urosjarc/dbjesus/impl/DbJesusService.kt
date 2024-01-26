package com.urosjarc.dbjesus.impl

import com.urosjarc.dbjesus.Engine
import com.urosjarc.dbjesus.Serializer
import com.urosjarc.dbjesus.Service
import com.urosjarc.dbjesus.domain.queries.Page
import com.urosjarc.dbjesus.domain.queries.Query
import com.urosjarc.dbjesus.domain.queries.QueryBuilder
import kotlin.reflect.KClass

class DbJesusService(
    override val eng: Engine,
    override val ser: Serializer,
) : Service<Int> {
    override fun <T : Any> createTable(kclass: KClass<T>): Int {
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

    override fun <T : Any> updateTable(obj: T): Int {
        val query = this.ser.updateQuery(obj = obj)
        val pQuery = this.eng.prepareQuery(query = query)
        return this.eng.executeUpdate(pQuery = pQuery)
    }

    override fun <T : Any> query(kclass: KClass<T>, getEscapedQuery: (addEncoders: QueryBuilder) -> Query): List<T> {
//        val query = this.ser.query(getEscapedQuery = getEscapedQuery)
//        val pQuery = this.eng.prepareQuery(query = query)
//        return this.eng.executeQuery(pQuery = pQuery) {
//            this.ser.mapper.decode(resultSet = it, kclass = kclass)
//        }
        return listOf()
    }
}
