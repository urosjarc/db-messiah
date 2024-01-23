package com.urosjarc.dbjesus

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.apache.logging.log4j.kotlin.logger
import java.sql.Connection
import java.sql.ResultSet
import java.sql.SQLException
import java.sql.Statement


abstract class SqlEngine(config: HikariConfig) {

    val log = this.logger()
    val dataSource = HikariDataSource(config)

    init {
        if (this.dataSource.isClosed && !this.dataSource.isRunning) {
            throw Exception("Database source is closed or not running!")
        }
    }

    private val connection
        get(): Connection? {
            try {
                return this.dataSource.connection
            } catch (e: SQLException) {
                this.log.fatal(e)
                return null
            }
        }

    fun <T> exec(sql: String, decode: (rs: ResultSet) -> T): MutableList<T> {
        this.log.debug("\n\n$sql\n\n")
        val objs = mutableListOf<T>()
        try {
            val stmt = this.connection!!.createStatement()
            val rs = stmt.executeQuery(sql)
            while (rs.next()) {
                objs.add(decode(rs))
            }
        } catch (e: SQLException) {
            this.log.error(e)
        }
        return objs
    }

    fun execMany(sql: String, decodeMany: (rs: ResultSet) -> Unit) {
        this.log.debug("\n\n$sql\n\n")
        try {
            val stmt: Statement = this.connection!!.createStatement()
            var isResultSet = stmt.execute(sql)

            var count = 0
            while (true) {
                if (isResultSet) {
                    val rs = stmt.resultSet
                    decodeMany(rs)
                } else if (stmt.updateCount == -1) break
                count++
                isResultSet = stmt.moreResults
            }

        } catch (e: SQLException) {
            this.log.error(e)
        }
    }

}
