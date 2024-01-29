package com.urosjarc.dbmessiah.impl

import com.urosjarc.dbmessiah.Engine
import com.urosjarc.dbmessiah.Serializer
import com.urosjarc.dbmessiah.Service
import com.urosjarc.dbmessiah.domain.queries.Page
import com.urosjarc.dbmessiah.domain.queries.QueryBuilderInOut
import com.urosjarc.dbmessiah.domain.queries.QueryBuilderOut
import kotlin.reflect.KClass

open class DbMessiahService(
    override val eng: Engine,
    override val ser: Serializer,
) : Service {
    override fun <T : Any> drop(kclass: KClass<T>): Int {
        val query = this.ser.dropQuery(kclass = kclass)
        return this.eng.executeUpdate(query = query)
    }

    override fun <T : Any> create(kclass: KClass<T>): Int {
        val query = this.ser.createQuery(kclass = kclass)
        return this.eng.executeUpdate(query = query)
    }

    override fun <T : Any> select(kclass: KClass<T>): List<T> {
        val query = this.ser.selectQuery(kclass = kclass)
        return this.eng.executeQuery(query = query) {
            this.ser.mapper.decode(kclass = kclass, resultSet = it)
        }
    }

    override fun <T : Any, K : Any> select(kclass: KClass<T>, pk: K): T? {
        val query = this.ser.selectQuery(kclass = kclass, pk = pk)
        return this.eng.executeQuery(query = query) {
            this.ser.mapper.decode(kclass = kclass, resultSet = it)
        }.firstOrNull()
    }

    override fun <T : Any> select(kclass: KClass<T>, page: Page<T>): List<T> {
        val query = this.ser.selectQuery(kclass = kclass, page = page)
        return this.eng.executeQuery(query = query) {
            this.ser.mapper.decode(kclass = kclass, resultSet = it)
        }
    }

    override fun <T : Any> insert(obj: T): Boolean {
        val T = this.ser.mapper.getTableInfo(obj = obj)
        val query = this.ser.insertQuery(obj = obj)
        val id = this.eng.executeInsert(query = query, primaryKey = T.primaryKey.kprop) { rs, i -> rs.getInt(i) }
        T.primaryKey.setValue(obj = obj, value = id)
        return true
    }

    override fun <T : Any> update(obj: T): Boolean {
        val query = this.ser.updateQuery(obj = obj)
        return this.eng.executeUpdate(query = query) == 0
    }

    override fun <T : Any> delete(obj: T): Boolean {
        val T = this.ser.mapper.getTableInfo(obj = obj)
        val query = this.ser.deleteQuery(obj = obj)
        val deleted = this.eng.executeUpdate(query = query) == 1
        if (deleted) T.primaryKey.setValue(obj = obj, value = null)
        return deleted
    }

    override fun <OUT : Any> query(output: KClass<OUT>, getSql: (queryBuilder: QueryBuilderOut<Unit, OUT>) -> String): List<OUT> {
        val query = this.ser.selectQuery(output = output, getSql = getSql)
        return this.eng.executeQuery(query = query) {
            this.ser.mapper.decode(resultSet = it, output)
        }
    }

    override fun <IN : Any, OUT : Any> query(input: IN, output: KClass<OUT>, getSql: (queryBuilder: QueryBuilderInOut<IN, OUT>) -> String): List<OUT> {
        val query = this.ser.selectQuery(input = input, output = output, getSql = getSql)
        return this.eng.executeQuery(query = query) {
            this.ser.mapper.decode(resultSet = it, output)
        }
    }

}
