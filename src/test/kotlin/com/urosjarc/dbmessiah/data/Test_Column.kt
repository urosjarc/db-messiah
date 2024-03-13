package com.urosjarc.dbmessiah.data

import com.urosjarc.dbmessiah.exceptions.DbValueException
import com.urosjarc.dbmessiah.serializers.AllTS
import com.urosjarc.dbmessiah.serializers.NumberTS
import com.urosjarc.dbmessiah.serializers.StringTS
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.assertThrows
import java.sql.JDBCType
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty1
import kotlin.test.*

class Test_Column {

    private lateinit var table: TableInfo
    private lateinit var foreignColumnCanBeNull: ForeignColumn
    private lateinit var otherNotInitedColumn: OtherColumn
    private lateinit var primaryColumn: PrimaryColumn
    private lateinit var foreignColumn: ForeignColumn
    private lateinit var otherColumn: OtherColumn
    private lateinit var entity: Entity

    data class Entity(var id: Int, val property: String)
    data class Entity2(var text: String?)


    @BeforeEach
    @Suppress("UNCHECKED_CAST")
    fun init() {
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
            encoder = { ps, i, x -> ps.setString(i, x.toString()) },
            cascadeUpdate = false,
            cascadeDelete = false
        )
        foreignColumnCanBeNull = ForeignColumn(
            unique = true,
            kprop = Entity2::text as KProperty1<Any, Any?>,
            dbType = "VARCHAR",
            jdbcType = JDBCType.VARCHAR,
            decoder = { rs, i, _ -> rs.getString(i) },
            encoder = { ps, i, x -> ps.setString(i, x.toString()) },
            cascadeUpdate = false,
            cascadeDelete = false
        )
        primaryColumn = PrimaryColumn(
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
        this.table = TableInfo(
            schema = "Schema",
            kclass = Entity::class,
            primaryColumn = primaryColumn,
            foreignColumns = listOf(foreignColumn, foreignColumnCanBeNull),
            otherColumns = listOf(otherColumn),
            typeSerializers = AllTS.basic
        )
    }

    @Test
    fun `test kclass`() {
        assertEquals(actual = otherColumn.kclass, expected = String::class)
        assertEquals(actual = primaryColumn.kclass, expected = Int::class)
        assertEquals(actual = foreignColumn.kclass, expected = String::class)
    }

    @Test
    fun `test decodeInfo`() {
        assertEquals(actual = otherColumn.decodeInfo, expected = DecodeInfo(kclass = String::class, kparam = null, kprop = Entity::property))
        assertEquals(actual = primaryColumn.decodeInfo, expected = DecodeInfo(kclass = Int::class, kparam = null, kprop = Entity::id))
        assertEquals(actual = foreignColumn.decodeInfo, expected = DecodeInfo(kclass = String::class, kparam = null, kprop = Entity::property))
    }

    @Test
    fun `test queryValue`() {
        assertEquals(
            actual = otherColumn.queryValueFrom(this.entity),
            expected = QueryValue(name = "property", value = "Property", jdbcType = JDBCType.VARCHAR, encoder = StringTS.string(100).encoder)
        )
        assertEquals(
            actual = primaryColumn.queryValueFrom(this.entity),
            expected = QueryValue(name = "id", value = 23, jdbcType = JDBCType.INTEGER, encoder = NumberTS.int.encoder)
        )
        assertEquals(
            actual = foreignColumn.queryValueFrom(this.entity),
            expected = QueryValue(name = "property", value = "Property", jdbcType = JDBCType.VARCHAR, encoder = StringTS.string(100).encoder)
        )
    }

    @Test
    fun `test setValue`() {
        assertEquals(this.entity.id, 23)
        assertEquals(this.entity.property, "Property")

        val e = assertThrows<DbValueException> {
            otherColumn.setValue(obj = this.entity, value = "xxxx")
        }
        assertContains(
            charSequence = e.stackTraceToString(),
            "Trying to set property 'val com.urosjarc.dbmessiah.data.Test_Column.Entity.property: kotlin.String' to 'xxxx' but the property is probably immutable"
        )

        assertEquals(this.entity.id, 23)
        assertEquals(this.entity.property, "Property")

        primaryColumn.setValue(obj = this.entity, value = 32)

        assertEquals(this.entity.id, 32)
        assertEquals(this.entity.property, "Property")
    }

    @Test
    fun `test getValue`() {
        assertEquals(actual = otherColumn.getValue(this.entity), expected = "Property")
        assertEquals(actual = primaryColumn.getValue(this.entity), expected = 23)
        assertEquals(actual = foreignColumn.getValue(this.entity), expected = "Property")
    }

    @Test
    fun `test table`() {
        assertEquals(actual = otherColumn.table, expected = this.table)
        assertEquals(actual = primaryColumn.table, expected = this.table)
        assertEquals(actual = foreignColumn.table, expected = this.table)
    }

    @Test
    fun `test inited`() {
        assertTrue(actual = otherColumn.inited)
        assertTrue(actual = primaryColumn.inited)
        assertTrue(actual = foreignColumn.inited)
    }

    @Test
    fun `test name`() {
        assertEquals(expected = "property", actual = otherColumn.name)
        assertEquals(expected = "property", actual = foreignColumn.name)
        assertEquals(expected = "id", actual = primaryColumn.name)
    }

    @Test
    fun `test path`() {
        assertEquals(expected = "Schema.Entity.property", actual = otherColumn.path)
        assertEquals(expected = "Schema.Entity.property", actual = foreignColumn.path)
        assertEquals(expected = "Schema.Entity.id", actual = primaryColumn.path)
    }

    @Test
    fun `test equals()`() {
        assertTrue(otherColumn.equals(foreignColumn))
        assertFalse(otherColumn.equals(primaryColumn))
        assertFalse(foreignColumn.equals(primaryColumn))
    }

    @Test
    fun `test toString()`() {
        assertEquals(expected = "Column(name='property', dbType='VARCHAR', jdbcType='VARCHAR')", otherColumn.toString())
        assertEquals(expected = "Column(name='property', dbType='VARCHAR', jdbcType='VARCHAR')", foreignColumn.toString())
        assertEquals(expected = "Column(name='id', dbType='INT', jdbcType='INTEGER')", primaryColumn.toString())
    }

}
