package com.urosjarc.dbmessiah.domain.columns

import com.urosjarc.dbmessiah.domain.table.Escaper
import com.urosjarc.dbmessiah.domain.table.TableInfo
import com.urosjarc.dbmessiah.exceptions.ColumnException
import com.urosjarc.dbmessiah.types.AllTS
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows
import java.sql.JDBCType
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty1
import kotlin.test.*

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class Test_Column {

    private lateinit var foreignColumnCanBeNull: ForeignColumn
    private lateinit var otherNotInitedColumn: OtherColumn
    private lateinit var primaryColumn: PrimaryColumn
    private lateinit var foreignColumn: ForeignColumn
    private lateinit var otherColumn: OtherColumn
    private lateinit var entity: Entity
    private lateinit var escaper: Escaper

    data class Entity(var id: Int, val property: String)
    data class Entity2(var text: String?)


    @BeforeEach
    @Suppress("UNCHECKED_CAST")
    fun init_table() {
        entity = Entity(id = 23, property = "Property")

        otherColumn = OtherColumn(
            unique = true,
            kprop = Entity::property as KProperty1<Any, Any?>,
            dbType = "VARCHAR",
            jdbcType = JDBCType.VARCHAR,
            decoder = { rs, i, _ -> rs.getString(i) },
            encoder = { ps, i, x -> ps.setString(i, x.toString()) }
        )
        foreignColumn = ForeignColumn(
            unique = true,
            kprop = Entity::property as KProperty1<Any, Any?>,
            dbType = "VARCHAR",
            jdbcType = JDBCType.VARCHAR,
            decoder = { rs, i, _ -> rs.getString(i) },
            encoder = { ps, i, x -> ps.setString(i, x.toString()) }
        )
        foreignColumnCanBeNull = ForeignColumn(
            unique = true,
            kprop = Entity2::text as KProperty1<Any, Any?>,
            dbType = "VARCHAR",
            jdbcType = JDBCType.VARCHAR,
            decoder = { rs, i, _ -> rs.getString(i) },
            encoder = { ps, i, x -> ps.setString(i, x.toString()) }
        )
        primaryColumn = PrimaryColumn(
            autoIncrement = true,
            kprop = Entity::id as KMutableProperty1<Any, Any?>,
            dbType = "INT",
            jdbcType = JDBCType.INTEGER,
            decoder = { rs, i, _ -> rs.getString(i) },
            encoder = { ps, i, x -> ps.setString(i, x.toString()) }
        )

        //Failed column which is not inited
        otherNotInitedColumn = OtherColumn(
            unique = true,
            kprop = Entity::property as KProperty1<Any, Any?>,
            dbType = "dbType",
            jdbcType = JDBCType.VARCHAR,
            decoder = { rs, i, _ -> rs.getString(i) },
            encoder = { ps, i, x -> ps.setString(i, x.toString()) }
        )
        escaper = Escaper(
            type = Escaper.Type.SINGLE_QUOTES,
            joinStr = "."
        )
        TableInfo(
            escaper = escaper,
            schema = "Schema",
            kclass = Entity::class,
            primaryKey = primaryColumn,
            foreignKeys = listOf(foreignColumn, foreignColumnCanBeNull),
            otherColumns = listOf(otherColumn),
            serializers = AllTS.basic
        )
    }

    @Test
    fun `test kclass`() {
        assertEquals(expected = String::class, actual = otherColumn.kclass)
        assertEquals(expected = String::class, actual = foreignColumn.kclass)
        assertEquals(expected = Int::class, actual = primaryColumn.kclass)
    }

    @Test
    fun `test name`() {
        assertEquals(expected = "'property'", actual = otherColumn.name)
        assertEquals(expected = "'property'", actual = foreignColumn.name)
        assertEquals(expected = "'id'", actual = primaryColumn.name)
    }

    @Test
    fun `test path`() {
        assertEquals(expected = "'Schema'.'Entity'.'property'", actual = otherColumn.path)
        assertEquals(expected = "'Schema'.'Entity'.'property'", actual = foreignColumn.path)
        assertEquals(expected = "'Schema'.'Entity'.'id'", actual = primaryColumn.path)
    }

    @Test
    fun `test hash`() {
        assertEquals(expected = otherColumn.hash, actual = foreignColumn.hash)
        assertNotEquals(illegal = otherColumn.hash, actual = primaryColumn.hash)
        assertNotEquals(illegal = foreignColumn.hash, actual = primaryColumn.hash)
    }

    @Test
    fun `test inited`() {
        assertTrue(actual = otherColumn.inited)
        assertTrue(actual = primaryColumn.inited)
        assertFalse(actual = foreignColumn.inited)

        foreignColumn.foreignTable = foreignColumn.table.copy()

        assertTrue(actual = foreignColumn.inited)
    }

    @Test
    fun `test ForeignColumn isNull`(){
        assertTrue(actual = foreignColumn.notNull)
        assertFalse(actual = foreignColumnCanBeNull.notNull)
    }

    @Test
    fun `test equals()`() {
        assertEquals(expected = otherColumn, actual = foreignColumn)
        assertFalse(otherColumn.equals(primaryColumn))
        assertFalse(foreignColumn.equals(primaryColumn))
    }

    @Test
    fun `test hashCode()`() {
        assertEquals(expected = otherColumn.hashCode(), actual = foreignColumn.hashCode())
        assertNotEquals(illegal = otherColumn.hashCode(), actual = primaryColumn.hashCode())
        assertNotEquals(illegal = foreignColumn.hashCode(), actual = primaryColumn.hashCode())
    }

    @Test
    fun `test toString()`() {
        assertEquals(expected = "Column(name='property', dbType='VARCHAR', jdbcType='VARCHAR')", otherColumn.toString())
        assertEquals(expected = "Column(name='property', dbType='VARCHAR', jdbcType='VARCHAR')", foreignColumn.toString())
        assertEquals(expected = "Column(name='id', dbType='INT', jdbcType='INTEGER')", primaryColumn.toString())
    }

    @Test
    fun `test queryValue()`() {
        val obj1 = Entity(id = 1, property = "property1")
        val obj2 = Entity(id = 2, property = "property2")
        val obj3 = Entity(id = 3, property = "property3")


        val q1 = otherColumn.queryValue(obj = obj1)
        assertEquals(actual = q1.name, expected = "'property'")
        assertEquals(actual = q1.value, expected = obj1.property)
        assertEquals(actual = q1.jdbcType, expected = JDBCType.VARCHAR)

        val q2 = foreignColumn.queryValue(obj = obj2)
        assertEquals(actual = q2.name, expected = "'property'")
        assertEquals(actual = q2.value, expected = obj2.property)
        assertEquals(actual = q2.jdbcType, expected = JDBCType.VARCHAR)

        val q3 = primaryColumn.queryValue(obj = obj3)
        assertEquals(actual = q3.name, expected = "'id'")
        assertEquals(actual = q3.value, expected = obj3.id)
        assertEquals(actual = q3.jdbcType, expected = JDBCType.INTEGER)
    }

    @Test
    fun `test setValue()`() {
        val obj1 = Entity(id = 1, property = "property1")
        val obj2 = Entity2(text = "text1")

        //Setting imutable property
        val exc1 = assertThrows<ColumnException> {
            otherColumn.setValue(obj = obj1, value = "XXX")
        }
        assertContains(charSequence = exc1.message.toString(), other = "column is immutable")

        //Setting imutable property
        val exc2 = assertThrows<ColumnException> {
            foreignColumn.setValue(obj = obj1, value = "XXX")
        }
        assertContains(charSequence = exc2.message.toString(),other = "column is immutable")

        //Setting incompatible type
        val exc3 = assertThrows<ColumnException> {
            primaryColumn.setValue(obj = obj1, value = "XXX")
        }
        assertContains(charSequence = exc3.message.toString(),other = "incompatible types")

        //Setting missing property
        val exc4 = assertThrows<ColumnException> {
            primaryColumn.setValue(obj = obj2, value = "XXX")
        }
        assertContains(charSequence = exc4.message.toString(),other = "missing matching property")


        primaryColumn.setValue(obj = obj1, value = 1234)
        assertEquals(actual = obj1, expected = Entity(id=1234, property = "property1"))

        foreignColumnCanBeNull.setValue(obj = obj2, value = null)
        assertEquals(actual = obj2, expected = Entity2(text = null))
    }

    @Test
    fun `test getValue()`(){
        val obj = Entity(id = 1, property = "property1")
        val obj2 = Entity2(text = "text1")

        assertEquals(actual = otherColumn.getValue(obj), expected = obj.property)
        assertEquals(actual = foreignColumn.getValue(obj), expected = obj.property)
        assertEquals(actual = primaryColumn.getValue(obj), expected = obj.id)
        assertEquals(actual = foreignColumnCanBeNull.getValue(obj2), expected = obj2.text)
    }

}
