package com.urosjarc.dbmessiah.impl

import com.urosjarc.dbmessiah.DbMessiahService
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
            sqliteConfig = HikariConfig().apply {
                jdbcUrl = "jdbc:sqlite::memory:"
                username = null
                password = null
            }

            serializer = SqliteMessiahSerializer(
                injectTestElements = true,
            )
            service = DbMessiahService(
                config = sqliteConfig,
                serializer = serializer
            )
        }
    }

    @Test
    fun `test sqlite crud cycle()`() {
        TestService(service = service).apply {
            this.test_crud_cycle(10)
        }
    }
}
