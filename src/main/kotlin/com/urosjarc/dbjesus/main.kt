package com.urosjarc.dbjesus

import com.urosjarc.dbjesus.domain.Table
import com.urosjarc.dbjesus.impl.DbJesusEngine
import com.urosjarc.dbjesus.impl.DbJesusService
import com.urosjarc.dbjesus.serializers.MariaDbSerializer
import com.urosjarc.dbjesus.serializers.basicDbTypeSerializers
import com.zaxxer.hikari.HikariConfig

data class Entity2(
    val id: Int,
    val name: String,
    val username: String,
    val age: Int,
    val money: Float
)
data class Entity(
    val id: Int,
    val name: String,
    val username: String,
    val age: Int,
    val money: Float
)
fun main() {

    val config = HikariConfig().also {
        it.jdbcUrl = "jdbc:sqlite:/home/urosjarc/vcs/db-jesus/src/test/resources/chinook.sqlite"
        it.username = null
        it.password = null
    }


    val serializer = MariaDbSerializer(
        globalSerializers = basicDbTypeSerializers,
        tables = listOf(
            Table(primaryKey = Entity::id),
            Table(primaryKey = Entity2::id)
        )
    )

    val service = DbJesusService(
        eng = DbJesusEngine(config = config),
        ser = serializer
    )

    service.createTable(kclass = Entity::class)
}
