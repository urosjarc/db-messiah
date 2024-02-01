package com.urosjarc.dbmessiah

import com.urosjarc.dbmessiah.impl.sqlite.SqliteMessiahSerializer
import com.urosjarc.dbmessiah.tests.TestService
import com.zaxxer.hikari.HikariConfig
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.io.File


@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class Test_Sqlite {

    companion object {
        private lateinit var service: DbMessiahService
        private lateinit var serializer: SqliteMessiahSerializer
        private lateinit var sqliteConfig: HikariConfig

        @JvmStatic
        @BeforeAll
        fun init() {
            this.sqliteConfig = HikariConfig().apply {
                jdbcUrl = "jdbc:sqlite:${File("src/e2e/resources/chinook.sqlite").absolutePath}"
                username = null
                password = null
            }

            this.serializer = SqliteMessiahSerializer(
                injectTestElements = true,
            )
            service = DbMessiahService(
                config = sqliteConfig,
                serializer = serializer
            )
        }
    }

    @Test
    fun `test crud cycle()`() {
        TestService(service = service).apply {
            this.test_crud_cycle(10)
        }
    }
}
