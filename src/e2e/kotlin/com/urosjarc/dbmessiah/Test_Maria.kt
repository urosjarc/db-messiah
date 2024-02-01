package com.urosjarc.dbmessiah

import com.urosjarc.dbmessiah.impl.sqlite.MariaSerializer
import com.zaxxer.hikari.HikariConfig
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test


class Test_Maria {

    companion object {
        private lateinit var service: DbMessiahService
        private lateinit var serializer: MariaSerializer
        private lateinit var sqliteConfig: HikariConfig

        @JvmStatic
        @BeforeAll
        fun init() {
            sqliteConfig = HikariConfig().apply {
                jdbcUrl = "jdbc:mariadb://localhost:3306"
                username = "root"
                password = "root"
            }

            serializer = MariaSerializer(
            )
            service = DbMessiahService(
                config = sqliteConfig,
                serializer = serializer
            )
        }
    }

    @Test
    fun `test maria crud cycle()`() {
        service.query {
            it.execute { "CREATE SCHEMA IF NOT EXISTS main" }
            it.execute { "SET GLOBAL FOREIGN_KEY_CHECKS=0;" }
            it.execute { "SET GLOBAL TRANSACTION ISOLATION LEVEL SERIALIZABLE;" }
        }

    }
}
