package com.urosjarc.dbjesus

import com.urosjarc.dbjesus.domain.DbTypeSerializer
import com.urosjarc.dbjesus.domain.ObjProperties
import com.urosjarc.dbjesus.domain.ObjProperty
import com.urosjarc.dbjesus.exceptions.DbMapperException
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
class Test_DbMapper {

    val mapper = DbMapper(dbTypeSerializers = basicDbTypeSerializers)

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
        assertThrows<DbMapperException> {
            this.mapper.getDbType(kclass = JDBCType::class as KClass<Any>)
        }
    }

    @Test
    fun `test getDbTypeSerializer`() {
        assertEquals<Any>(
            expected = stringSerializer(), actual = this.mapper.getDbTypeSerializer(kclass = String::class as KClass<Any>)
        )
        assertThrows<DbMapperException> {
            this.mapper.getDbTypeSerializer(kclass = JDBCType::class as KClass<Any>)
        }
    }

    @Test
    fun `test getObjProperties`() {
        val entity = Entity()

        val actual = this.mapper.getObjProperties(obj = entity, primaryKey = "int")
        actual.list.sortBy { it.name }

        val expected = ObjProperties(
            primaryKey = ObjProperty(name = "int", value = entity.int, property = Entity::int as KProperty1<Any, *>, serializer = intSerializer as DbTypeSerializer<Any>), list = listOf(
                ObjProperty(
                    name = Entity::parentField.name, value = entity.parentField, property = Entity::parentField as KProperty1<Any, *>, serializer = stringSerializer() as DbTypeSerializer<Any>
                ),
                ObjProperty(name = Entity::string.name, value = entity.string, property = Entity::string as KProperty1<Any, *>, serializer = stringSerializer() as DbTypeSerializer<Any>),
                ObjProperty(name = Entity::float.name, value = entity.float, property = Entity::float as KProperty1<Any, *>, serializer = floatSerializer as DbTypeSerializer<Any>),
                ObjProperty(name = Entity::double.name, value = entity.double, property = Entity::double as KProperty1<Any, *>, serializer = doubleSerializer as DbTypeSerializer<Any>),
                ObjProperty(name = Entity::boolean.name, value = entity.boolean, property = Entity::boolean as KProperty1<Any, *>, serializer = booleanSerializer as DbTypeSerializer<Any>),
                ObjProperty(name = Entity::char.name, value = entity.char, property = Entity::char as KProperty1<Any, *>, serializer = charSerializer as DbTypeSerializer<Any>)
            ).sortedBy { it.name }.toMutableList()
        )

        assertEquals(actual = actual.primaryKey, expected = expected.primaryKey)
        assertEquals(actual = actual.list.size, expected = expected.list.size)
        assertContentEquals(actual = actual.list, expected = expected.list)
    }

    @Test
    fun `test getObjProperties missing primary Key`() {
        val entity = Entity()

        val e = assertThrows<DbMapperException> {
            this.mapper.getObjProperties(obj = entity, primaryKey = "XXX")
        }
        assertTrue(e.message?.startsWith("Primary key 'XXX' not found") == true, e.message)
    }

    @Test
    fun `test getObjProperties missing serializer`() {
        class Entity2(val entity: Entity = Entity())

        val e = assertThrows<DbMapperException> {
            this.mapper.getObjProperties(obj = Entity2(), primaryKey = "XXX")
        }
        assertTrue(e.message?.startsWith("Serializer missing") == true, e.message)
    }


}
