package com.urosjarc.dbmessiah.impl

import com.urosjarc.dbmessiah.DbMessiahService
import com.urosjarc.dbmessiah.impl.sqlite.MariaMessiahSerializer
import com.zaxxer.hikari.HikariConfig
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance


@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class Test_Maria {

    companion object {
        private lateinit var service: DbMessiahService
        private lateinit var serializer: MariaMessiahSerializer
        private lateinit var sqliteConfig: HikariConfig

        @JvmStatic
        @BeforeAll
        fun init() {
            sqliteConfig = HikariConfig().apply {
                jdbcUrl = "jdbc:mariadb://localhost:3306"
                username = "root"
                password = "root"
            }

            serializer = MariaMessiahSerializer(
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
