package com.urosjarc.dbjesus.domain

import com.mockrunner.jdbc.BasicJDBCTestCaseAdapter
import com.mockrunner.mock.jdbc.MockResultSet
import com.urosjarc.dbjesus.mappings.baseMappings
import com.urosjarc.dbjesus.mappings.floatMapping
import com.urosjarc.dbjesus.mappings.stringMapping
import org.junit.jupiter.api.TestInstance
import java.sql.ResultSetMetaData
import java.util.HashMap
import kotlin.test.*


@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class Test_SqlMapper : BasicJDBCTestCaseAdapter() {

    val sqlMapper = SqlMapper(sqlMappings = baseMappings)

    data class Entity(
        val string: String = "String",
        val int: Int = 123,
        val float: Float = 123.123f,
        val double: Double = 123.123,
        val boolean: Boolean = true,
        val char: Char = 'C',
    ) {
        val map get(): HashMap<String, Any?> {
            val hashMap = HashMap<String, Any?>().also {
                it["string"] = this.string
                it["int"] = this.int
                it["float"] = this.float
                it["double"] = this.double
                it["boolean"] = this.boolean
                it["char"] = this.char
            }
            return hashMap
        }
    }

    val e1 = Entity()

    private fun entityResultSet(): MockResultSet {
        val connection = jdbcMockObjectFactory.mockConnection
        val statementHandler = connection.statementResultSetHandler
        val result = statementHandler.createResultSet()

        result.addColumns(e1.map.keys)
        result.addRow(e1.map.values.toList())
        result.setResultSetMetaData()
        result.metaData = ResultSetMetaData()

        return result
    }

    private fun entityResultSetFail(): MockResultSet {
        val connection = jdbcMockObjectFactory.mockConnection
        val statementHandler = connection.statementResultSetHandler
        val result = statementHandler.createResultSet()
        return result
    }

    @Test
    fun `test getDbType pass`() {
        assertEquals(
            expected = stringMapping().dbType,
            actual = sqlMapper.getDbType(String::class)
        )
    }

    @Test
    fun `test getDbType fail`() {
        assertNotEquals(
            illegal = floatMapping.dbType,
            actual = sqlMapper.getDbType(String::class)
        )
    }

    @Test
    fun `test decode pass`() {
        val rs = this.entityResultSet()
        rs.addRow(listOf(e1.string, e1.int, e1.float, e1.double, e1.boolean, e1.char))
        val entity1 = sqlMapper.decode(kclass = Entity::class, resultSet = rs)
        assertEquals(expected = e1, actual = entity1)
    }
    @Test
    fun `test decode null pass`() {
        val rs = this.entityResultSetFail()
        rs.addRow(listOf(e1.string, e1.int, null, e1.double, e1.boolean, e1.char))
        val entity1 = sqlMapper.decode(kclass = Entity::class, resultSet = rs)
        assertEquals(expected = e1, actual = entity1)
    }
}
