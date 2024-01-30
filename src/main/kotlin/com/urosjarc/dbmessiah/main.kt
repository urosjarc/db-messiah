package com.urosjarc.dbmessiah

import com.codahale.metrics.MetricRegistry
import com.urosjarc.dbmessiah.domain.columns.C
import com.urosjarc.dbmessiah.domain.schema.Schema
import com.urosjarc.dbmessiah.domain.table.Table
import com.urosjarc.dbmessiah.impl.sqlite.SqliteMessiahSerializer
import com.urosjarc.dbmessiah.tests.TestService
import com.urosjarc.dbmessiah.types.AllTS
import com.zaxxer.hikari.HikariConfig


fun main() {

    val config = HikariConfig().apply {
        jdbcUrl = "jdbc:sqlite:/home/urosjarc/vcs/db-jesus/src/test/resources/chinook.sqlite"
        username = null; password = null
        metricRegistry = MetricRegistry()
    }

    val serializer = SqliteMessiahSerializer(
        injectTestElements = true,
    )

    val service = DbMessiahService(
        config = config,
        serializer = serializer
    )

    TestService(service = service).apply {
        this.test_crud_cycle(200)
    }

}
