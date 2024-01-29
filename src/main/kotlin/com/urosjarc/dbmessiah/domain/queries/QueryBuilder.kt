package com.urosjarc.dbmessiah.domain.queries

import com.urosjarc.dbmessiah.Mapper
import org.apache.logging.log4j.kotlin.logger

open class QueryBuilder<IN>(
    open val input: IN,
    val mapper: Mapper,
) {

    val log = this.logger()
    val queryValues: MutableList<QueryValue> = mutableListOf()

    fun build(sql: String) = Query(sql = sql, values = this.queryValues.toTypedArray())

}
