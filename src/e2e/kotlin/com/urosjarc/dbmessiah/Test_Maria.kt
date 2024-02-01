package com.urosjarc.dbmessiah

import com.urosjarc.dbmessiah.impl.sqlite.MariaMessiahSerializer
import com.urosjarc.dbmessiah.impl.sqlite.SqliteMessiahSerializer
import com.urosjarc.dbmessiah.tests.TestService
import com.zaxxer.hikari.HikariConfig
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.io.File


@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class Test_Maria {

    companion object {
        private lateinit var service: DbMessiahService
        private lateinit var serializer: MariaMessiahSerializer
        private lateinit var sqliteConfig: HikariConfig

        @JvmStatic
        @BeforeAll
        fun init() {
            this.sqliteConfig = HikariConfig().apply {
                jdbcUrl = "jdbc:mariadb://0.0.0.0:3306"
                username = "root"
                password = "root"
            }

            this.serializer = MariaMessiahSerializer(
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
        TestService(service = service).apply {
            this.test_crud_cycle(10)
        }
    }
}
