package com.urosjarc.dbjesus

import com.urosjarc.dbjesus.domain.columns.C
import com.urosjarc.dbjesus.domain.table.Table
import com.urosjarc.dbjesus.impl.DbJesusEngine
import com.urosjarc.dbjesus.impl.DbJesusService
import com.urosjarc.dbjesus.serializers.SqliteSerializer
import com.urosjarc.dbjesus.serializers.basicDbTypeSerializers
import com.zaxxer.hikari.HikariConfig
import kotlin.reflect.KProperty1

data class Entity2(
    val id_entity2: Int,
    val name: String,
    val username: String,
    val age: Int,
    val money: Float
)

data class Entity(
    val id_entity: Int,
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

    val foreignKeys = mapOf<KProperty1<*, *>, Any>(
        Entity::age to Entity::class,
        Entity::age to String::class
    )

    val serializer = SqliteSerializer(
        globalSerializers = basicDbTypeSerializers,
        tables = listOf(
            Table(
                primaryKey = Entity::id_entity, foreignKeys = listOf(
                    Entity::name to Entity2::class
                )
            ),
            Table(
                primaryKey = Entity2::id_entity2,
                foreignKeys = listOf(
                    Entity2::age to String::class
                ),
                constraints = listOf(
                    Entity2::age to listOf(C.UNIQUE, C.AUTO_INC),
                    Entity2::name to listOf(C.UNIQUE, C.AUTO_INC)
                )
            )
        )
    )

    val service = DbJesusService(
        eng = DbJesusEngine(config = config),
        ser = serializer
    )

    service.createTable(kclass = Entity::class)
}
