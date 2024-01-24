package com.urosjarc.dbjesus.domain

import com.urosjarc.dbjesus.DbMapper
import com.urosjarc.dbjesus.exceptions.DbMappingException
import com.urosjarc.dbjesus.impl.basicDbTypeSerializers
import com.urosjarc.dbjesus.impl.stringSerializer
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows
import java.sql.JDBCType
import kotlin.reflect.KClass
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue


@Suppress("UNCHECKED_CAST")
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class Test_DbMapper {

    val mapper = DbMapper(dbTypeSerializers = basicDbTypeSerializers)

    data class Entity(
        val string: String = "String",
        val int: Int = 123,
        val float: Float = 123.123f,
        val double: Double = 123.123,
        val boolean: Boolean = true,
        val char: Char = 'C',
    ) {
        val map
            get(): HashMap<String, Any?> {
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

    @Test
    fun `test getDbType`() {
        assertEquals(
            expected = stringSerializer().dbType,
            actual = this.mapper.getDbType(kclass = String::class as KClass<Any>)
        )
        assertThrows<DbMappingException> {
            this.mapper.getDbType(kclass = JDBCType::class as KClass<Any>)
        }
    }

    @Test
    fun `test getDbTypeSerializer`() {
        assertEquals<Any>(
            expected = stringSerializer(),
            actual = this.mapper.getDbTypeSerializer(kclass = String::class as KClass<Any>)
        )
        assertThrows<DbMappingException> {
            this.mapper.getDbTypeSerializer(kclass = JDBCType::class as KClass<Any>)
        }
    }
}
