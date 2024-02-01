package com.urosjarc.dbmessiah.impl

import com.urosjarc.dbmessiah.*
import com.urosjarc.dbmessiah.impl.sqlite.SqliteMessiahSerializer
import com.urosjarc.dbmessiah.types.AllTS
import com.zaxxer.hikari.HikariConfig
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance


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
                schemas = listOf(testSchema),
                globalSerializers = AllTS.basic,
                globalOutputs = listOf(TestOutput::class),
                globalInputs = listOf(TestInput::class)
            )
            service = DbMessiahService(
                config = sqliteConfig,
                serializer = serializer
            )
        }
    }

    @Test
    fun `test sqlite crud cycle()`() {
        val obj = TestService(service = service).apply {
            this.`test drop`()
        }
    }
}
