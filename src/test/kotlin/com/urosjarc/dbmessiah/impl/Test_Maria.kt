package com.urosjarc.dbmessiah.impl

import com.urosjarc.dbmessiah.DbMessiahService
import com.urosjarc.dbmessiah.impl.sqlite.MariaMessiahSerializer
import com.urosjarc.dbmessiah.tests.TestService
import com.zaxxer.hikari.HikariConfig
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInstance
import kotlin.test.Test


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
                jdbcUrl = "jdbc:mariadb://0.0.0.0:3306"
                username = "root"
                password = "root"
            }

            serializer = MariaMessiahSerializer(
                injectTestElements = true,
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
            it.query { "SET GLOBAL FOREIGN_KEY_CHECKS=0;" }
            it.query { "SET GLOBAL TRANSACTION ISOLATION LEVEL SERIALIZABLE;" }
        }

        TestService(service = service).apply {
            this.test_crud_cycle(3)
        }
    }
}
