package com.urosjarc.dbmessiah

import com.codahale.metrics.MetricRegistry
import com.urosjarc.dbmessiah.domain.columns.C
import com.urosjarc.dbmessiah.domain.schema.Schema
import com.urosjarc.dbmessiah.domain.table.Table
import com.urosjarc.dbmessiah.domain.test.TestInput
import com.urosjarc.dbmessiah.domain.test.TestOutput
import com.urosjarc.dbmessiah.domain.test.TestTableParent
import com.urosjarc.dbmessiah.impl.sqlite.SqliteMessiahSerializer
import com.urosjarc.dbmessiah.types.AllTS
import com.zaxxer.hikari.HikariConfig

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

    val config = HikariConfig().apply {
        jdbcUrl = "jdbc:sqlite:/home/urosjarc/vcs/db-jesus/src/test/resources/chinook.sqlite"
        username = null; password = null
        metricRegistry = MetricRegistry()
    }

    val serializer = SqliteMessiahSerializer(
        testCRUD = true,
        globalSerializers = AllTS.basic,
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
        ),
        globalInputs = listOf(
            TestInput::class
        ),
        globalOutputs = listOf(
            TestOutput::class
        ),
    )

    val db = DbMessiahService(
        config = config,
        serializer = serializer
    )

    val obj = TestTableParent()
//    db.transaction {
//        it.drop(TestTableParent::class)
//        it.create(TestTableParent::class)
//        it.insert(obj = obj)
//        it.insert(obj = obj.copy(col13 = "Uros Jarc"))
//    }
    db.query {
        it.drop(TestTableParent::class)
        it.create(TestTableParent::class)
        it.insert(obj = obj)
        it.insert(obj = obj.copy(id=null, col13 = "ASDFASDF"))
    }

//    TestService(service = service).apply {
//        this.test_crud_cycle(200)
//    }

}
