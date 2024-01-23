package com.urosjarc.diysqlservice

import com.urosjarc.diysqlservice.domain.SqlMapper
import com.urosjarc.diysqlservice.impl.MariaSqlMapper
import com.urosjarc.diysqlservice.impl.baseMappings
import com.zaxxer.hikari.HikariConfig
import org.apache.logging.log4j.kotlin.logger
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance


@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class Test_MariaSqlMapper {

    val log = logger()

    val config = HikariConfig().apply {
        password = Env.MARIA_DB_PASSWORD
        username = Env.MARIA_DB_USERNAME
        jdbcUrl = Env.MARIA_DB_URL
    }

    val sqlMapper = SqlMapper(baseMappings)

    val service = MariaSqlMapper(config = config, sqlMapper = sqlMapper)

    data class Phone(
        val id: Int,
        val number: String
    )

    data class User(
        val id: Int,
        val id_phone: Phone,
        val money: Float,
        val username: String
    )

    @BeforeEach
    fun before_each() {
    }

    @AfterEach
    fun after_each() {
    }

    @Test
    fun test_db_service() {
        this.service.createTable()
    }
}
