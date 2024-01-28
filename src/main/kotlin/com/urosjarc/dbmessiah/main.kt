package com.urosjarc.dbmessiah

import com.urosjarc.dbmessiah.domain.columns.C
import com.urosjarc.dbmessiah.domain.schema.Schema
import com.urosjarc.dbmessiah.domain.table.Table
import com.urosjarc.dbmessiah.impl.DbMessiahEngine
import com.urosjarc.dbmessiah.impl.DbMessiahService
import com.urosjarc.dbmessiah.impl.basicDbTypeSerializers
import com.urosjarc.dbmessiah.sqlite.SqliteSerializer
import com.urosjarc.dbmessiah.sqlite.SqliteService
import com.zaxxer.hikari.HikariConfig
import kotlin.reflect.KMutableProperty1

data class Entity2(
    var id_entity2: Int,
    val name: String,
    val username: String,
    var age: Int,
    val money: Float
)

data class Entity(
    var id_entity: Int?,
    val name: String,
    var username: String,
    val age: Int,
    val money: Float
)

fun main() {

    val config = HikariConfig().also {
        it.jdbcUrl = "jdbc:sqlite:/home/urosjarc/vcs/db-jesus/src/test/resources/chinook.sqlite"
        it.username = null
        it.password = null
    }

    val test: List<KMutableProperty1<out Any, out Int?>> = listOf(
        Entity::id_entity,
        Entity2::id_entity2
    )
    val serializer = SqliteSerializer(
        escaper = "'",
        globalSerializers = basicDbTypeSerializers,
        schemas = listOf(
            Schema(
                name = "main",
                tables = listOf(
                    Table(primaryKey = Entity::id_entity),
                    Table(
                        primaryKey = Entity2::id_entity2,
                        constraints = listOf(
                            Entity2::age to listOf(C.UNIQUE, C.AUTO_INC),
                            Entity2::name to listOf(C.UNIQUE, C.AUTO_INC)
                        )
                    )
                )
            )
        )
    )

    val service = SqliteService(
        eng = DbMessiahEngine(config = config),
        ser = serializer
    )
    val e = Entity(id_entity = null, name = "Uros", username = "urosjarc", age = 31, money = 0f)
    val e2 = e.copy(name = "asdfasdfasdfsdf")
    service.dropTable(kclass = Entity::class)
    service.createTable(kclass = Entity::class)
    service.insertTable(e)
    service.insertTable(e2)
    e.username = "asdfasdf"
    println(service.updateTable(e))

    println(service.selectTable(kclass = Entity::class))
}
