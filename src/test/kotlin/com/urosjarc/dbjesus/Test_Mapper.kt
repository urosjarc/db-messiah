package com.urosjarc.dbjesus

import com.urosjarc.dbjesus.domain.serialization.TypeSerializer
import com.urosjarc.dbjesus.exceptions.MapperException
import com.urosjarc.dbjesus.serializers.*
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows
import java.sql.JDBCType
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals


@Suppress("UNCHECKED_CAST")
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class Test_Mapper {

    val mapper = Mapper(globalSerializers = basicDbTypeSerializers)

    open class EntityParent(
        val parentField: String = "parentField"
    )

    data class Entity(
        val string: String = "String",
        val int: Int = 123,
        val float: Float = 123.123f,
        val double: Double? = null,
        val boolean: Boolean = true,
        val char: Char = 'C',
    ) : EntityParent()

    @Test
    fun `test getDbType`() {
        assertEquals(
            expected = stringSerializer().dbType, actual = this.mapper.getDbType(kclass = String::class as KClass<Any>)
        )
        assertThrows<MapperException> {
            this.mapper.getDbType(kclass = JDBCType::class as KClass<Any>)
        }
    }

    @Test
    fun `test getDbTypeSerializer`() {
        assertEquals<Any>(
            expected = stringSerializer(), actual = this.mapper.getSerializer(kclass = String::class as KClass<Any>)
        )
        assertThrows<MapperException> {
            this.mapper.getSerializer(kclass = JDBCType::class as KClass<Any>)
        }
    }

    @Test
    fun `test getObjProperties`() {
        val entity = Entity()

        val actual = this.mapper.getObjProperties(obj = entity, primaryKey = "int") //With primary key
        val actual2 = this.mapper.getObjProperties(obj = entity, primaryKey = null) //Without primary key

        actual.list.sortBy { it.name }
        actual2.list.sortBy { it.name }

        val expected = ObjProperties(
            primaryKey = ObjProperty(name = "int", value = entity.int, property = Entity::int as KProperty1<Any, *>, serializer = intSerializer as TypeSerializer<Any>), list = listOf(
                ObjProperty(
                    name = Entity::parentField.name, value = entity.parentField, property = Entity::parentField as KProperty1<Any, *>, serializer = stringSerializer() as TypeSerializer<Any>
                ),
                ObjProperty(name = Entity::string.name, value = entity.string, property = Entity::string as KProperty1<Any, *>, serializer = stringSerializer() as TypeSerializer<Any>),
                ObjProperty(name = Entity::float.name, value = entity.float, property = Entity::float as KProperty1<Any, *>, serializer = floatSerializer as TypeSerializer<Any>),
                ObjProperty(name = Entity::double.name, value = entity.double, property = Entity::double as KProperty1<Any, *>, serializer = doubleSerializer as TypeSerializer<Any>),
                ObjProperty(name = Entity::boolean.name, value = entity.boolean, property = Entity::boolean as KProperty1<Any, *>, serializer = booleanSerializer as TypeSerializer<Any>),
                ObjProperty(name = Entity::char.name, value = entity.char, property = Entity::char as KProperty1<Any, *>, serializer = charSerializer as TypeSerializer<Any>)
            ).sortedBy { it.name }.toMutableList()
        )

        val expected2 = ObjProperties(primaryKey = null, list = mutableListOf(expected.primaryKey).apply { addAll(expected.list) } as MutableList<ObjProperty>)

        assertEquals(actual = actual.primaryKey, expected = expected.primaryKey)
        assertEquals(actual = actual2.primaryKey, expected = expected2.primaryKey)

        assertEquals(actual = actual.list.size, expected = expected.list.size)
        assertEquals(actual = actual2.list.size, expected = expected2.list.size)

        assertContentEquals(actual = actual.list, expected = expected.list)
        assertContentEquals(actual = actual2.list, expected = expected2.list)
    }

    @Test
    fun `test getObjProperties missing primary Key`() {
        val entity = Entity()

        val e = assertThrows<MapperException> {
            this.mapper.getObjProperties(obj = entity, primaryKey = "XXX")
        }
        assertTrue(e.message?.startsWith("Primary key 'XXX' not found") == true, e.message)
    }

    @Test
    fun `test getObjProperties missing serializer`() {
        class Entity2(val entity: Entity = Entity())

        val e = assertThrows<MapperException> {
            this.mapper.getObjProperties(obj = Entity2(), primaryKey = "XXX")
        }
        assertTrue(e.message?.startsWith("Serializer missing") == true, e.message)
    }


}
