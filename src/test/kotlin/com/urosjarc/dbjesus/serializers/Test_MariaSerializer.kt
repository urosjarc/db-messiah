package com.urosjarc.dbjesus.serializers

import com.urosjarc.dbjesus.domain.serialization.Encoder
import com.urosjarc.dbjesus.domain.queries.InsertQuery
import com.urosjarc.dbjesus.domain.queries.Page
import com.urosjarc.dbjesus.domain.queries.Query
import org.junit.jupiter.api.TestInstance
import kotlin.test.Test
import kotlin.test.assertEquals


@Suppress("UNCHECKED_CAST")
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class Test_MariaSerializer {

    val seri = MariaDbSerializer()

    open class EntityParent(
        val parentField: String = "parentField"
    )

    data class Entity(
        val id: Int = 32,
        val string: String = "String",
        val int: Int = 123,
        val float: Float = 123.123f,
        val double: Double? = null,
        val boolean: Boolean = true,
        val char: Char = 'C',
    ) : EntityParent()

    data class Output(
        val id: Int = 32,
        val string: String = "String",
        val int: Int = 123,
        val float: Float = 123.123f,
        val double: Double? = null,
        val boolean: Boolean = true,
        val char: Char = 'C',
    ) : EntityParent()

    @Test
    fun `test createQuery`() {
        val actual = this.seri.createQuery(kclass = Entity::class)
        val expected = Query(
            sql = "CREATE OR REPLACE TABLE Entity (boolean BOOLEAN, char CHAR, double DOUBLE, float FLOAT, id INTEGER PRIMARY KEY AUTOINCREMENT, PRIMARY KEY(id), int INT, string TEXT, parentField TEXT);",
            encoders = mutableListOf(),
            values = mutableListOf(),
            jdbcTypes = mutableListOf()
        )
        assertEquals(actual = actual, expected = expected)
    }

    @Test
    fun `test selectAllQuery`() {
        val actual = this.seri.selectAllQuery(kclass = Entity::class)
        val expected = Query(
            sql = "SELECT * FROM Entity",
            encoders = mutableListOf(),
            values = mutableListOf(),
            jdbcTypes = mutableListOf()
        )
        assertEquals(actual = actual, expected = expected)
    }

    @Test
    fun `test selectPageQuery`() {
        val actual = this.seri.selectPageQuery(kclass = Entity::class, page = Page(number = 3, orderBy = Entity::parentField, limit = 11, sort = Page.Sort.ASC))
        val expected = Query(
            sql = "SELECT * FROM Entity ORDER BY parentField ASC LIMIT 11 OFFSET 33",
            encoders = mutableListOf(),
            values = mutableListOf(),
            jdbcTypes = mutableListOf()
        )
        assertEquals(actual = actual, expected = expected)
    }

    @Test
    fun `test selectOneQuery`() {
        val actual = this.seri.selectOneQuery(kclass = Entity::class, pkValue = 123)
        val expected = Query(
            sql = "SELECT * FROM Entity WHERE id=123",
            encoders = mutableListOf(),
            values = mutableListOf(),
            jdbcTypes = mutableListOf()
        )
        assertEquals(actual = actual, expected = expected)
    }

    @Test
    fun `test insertQuery`() {
        val obj = Entity()
        val actual = this.seri.insertQuery(obj = obj)
        val expected = InsertQuery(
            sql = "INSERT INTO Entity ('boolean', 'char', 'double', 'float', 'int', 'string', 'parentField') VALUES (?, ?, ?, ?, ?, ?, ?);",
            encoders = mutableListOf(
                booleanSerializer.encoder,
                charSerializer.encoder,
                doubleSerializer.encoder,
                floatSerializer.encoder,
                intSerializer.encoder,
                stringSerializer().encoder,
                stringSerializer().encoder
            ) as MutableList<Encoder<Any>>,
            values = mutableListOf(
                obj.boolean,
                obj.char,
                obj.double,
                obj.float,
                obj.int,
                obj.string,
                obj.parentField
            ),
            jdbcTypes = mutableListOf(
                booleanSerializer.jdbcType,
                charSerializer.jdbcType,
                doubleSerializer.jdbcType,
                floatSerializer.jdbcType,
                intSerializer.jdbcType,
                stringSerializer().jdbcType,
                stringSerializer().jdbcType
            )
        )
        assertEquals(actual = actual, expected = expected)
    }

    @Test
    fun `test updateQuery`() {
        val obj = Entity()
        val actual = this.seri.updateQuery(obj = obj)
        val expected = Query(
            sql = "UPDATE Entity SET 'boolean' = ?, 'char' = ?, 'double' = ?, 'float' = ?, 'int' = ?, 'string' = ?, 'parentField' = ? WHERE id=${obj.id}",
            encoders = mutableListOf(
                booleanSerializer.encoder,
                charSerializer.encoder,
                doubleSerializer.encoder,
                floatSerializer.encoder,
                intSerializer.encoder,
                stringSerializer().encoder,
                stringSerializer().encoder
            ) as MutableList<Encoder<Any>>,
            values = mutableListOf(
                obj.boolean,
                obj.char,
                obj.double,
                obj.float,
                obj.int,
                obj.string,
                obj.parentField
            ),
            jdbcTypes = mutableListOf(
                booleanSerializer.jdbcType,
                charSerializer.jdbcType,
                doubleSerializer.jdbcType,
                floatSerializer.jdbcType,
                intSerializer.jdbcType,
                stringSerializer().jdbcType,
                stringSerializer().jdbcType
            )
        )
        assertEquals(actual = actual, expected = expected)
    }

    @Test
    fun `test query`() {
        val obj = Entity()

        val actual: Query = this.seri.query {
            """
                select * from Employee;
            """
        }
    }
}
//val expected = Query(
//    sql = "CREATE OR REPLACE TABLE Entity (boolean BOOLEAN, char CHAR, double DOUBLE, float FLOAT, int INT, string TEXT, parentField TEXT);",
//    encoders = mutableListOf(
//        booleanSerializer.encoder,
//        charSerializer.encoder,
//        doubleSerializer.encoder,
//        floatSerializer.encoder,
//        intSerializer.encoder,
//        stringSerializer().encoder,
//        stringSerializer().encoder
//    ) as MutableList<Encoder<Any>>,
//    values = mutableListOf(
//        e.boolean,
//        e.char,
//        e.double,
//        e.float,
//        e.int,
//        e.string,
//        e.parentField,
//    ),
//    jdbcTypes = mutableListOf(
//        booleanSerializer.jdbcType,
//        charSerializer.jdbcType,
//        doubleSerializer.jdbcType,
//        floatSerializer.jdbcType,
//        intSerializer.jdbcType,
//        stringSerializer().jdbcType,
//        stringSerializer().jdbcType
//    )
//)
