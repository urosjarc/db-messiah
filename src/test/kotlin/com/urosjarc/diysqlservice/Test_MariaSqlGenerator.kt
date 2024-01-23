package com.urosjarc.diysqlservice

import com.urosjarc.diysqlservice.domain.SqlMapper
import com.urosjarc.diysqlservice.impl.MariaSqlGenerator
import com.urosjarc.diysqlservice.impl.baseMappings
import com.zaxxer.hikari.HikariConfig
import org.apache.logging.log4j.kotlin.logger
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import kotlin.test.assertEquals


@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class Test_MariaSqlGenerator {

    val log = logger()

    val config = HikariConfig().apply {
        password = Env.MARIA_DB_PASSWORD
        username = Env.MARIA_DB_USERNAME
        jdbcUrl = Env.MARIA_DB_URL
    }

    val sqlMapper = SqlMapper(baseMappings)

    val service = MariaSqlGenerator(sqlMapper = sqlMapper)

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
        assertEquals(
            "CREATE OR REPLACE TABLE User (" +
                    "id INT AUTO_INCREMENT, " +
                    "PRIMARY KEY(id), " +
                    "id_phone INT NOT NULL, " +
                    "constraint fk_type foreign key(id_phone) references Phone(id), " +
                    "money FLOAT, " +
                    "username TEXT" +
                    ");",
            this.service.createTable(kclass = User::class)
        )
    }
}
