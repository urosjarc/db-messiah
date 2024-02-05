package com.urosjarc.dbmessiah

import com.urosjarc.dbmessiah.domain.call.Procedure
import com.urosjarc.dbmessiah.domain.call.ProcedureArg
import com.urosjarc.dbmessiah.domain.columns.PrimaryColumn
import com.urosjarc.dbmessiah.domain.table.Table
import com.urosjarc.dbmessiah.domain.table.TableInfo
import com.urosjarc.dbmessiah.exceptions.MapperException
import com.urosjarc.dbmessiah.exceptions.SerializerException
import com.urosjarc.dbmessiah.types.AllTS
import com.urosjarc.dbmessiah.types.NumberTS
import com.urosjarc.dbmessiah.types.StringTS
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.assertThrows
import java.sql.JDBCType
import java.time.LocalDate
import kotlin.reflect.KFunction
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty1
import kotlin.reflect.full.primaryConstructor
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals

class Test_Db_Mapper {

    private lateinit var mapper: Mapper

    private data class Child(var pk: Int)
    private data class Parent(var pk: Int, val col: String)

    private open class Complex(open var pk: Int, name: Int, username: Int) {
        val realName = "$name"
        val realUsername = "$username"
    }

    private class Empty {
        var pk: Int = 23
    }

    private data class Exotic(var pk: Int = 23, val localDate: LocalDate)

    private inner class Inherited(
        override var pk: Int,
        var test: Float,
    ) : Complex(pk = pk, name = 1, username = 2) {
        val getter: String get() = "hello"
    }

    class ProcedureEmpty
    class ProcedureNotEmpty(val pk: Int, val col: String)
    class ProcedureNotRegistered()

    class Input(val pk: Int, val col: String)
    class Output(val pk: Int, val col: String)

    @BeforeEach
    fun init() {
        this.mapper = Mapper(
            schemas = listOf(
                Schema(
                    name = "main",
                    tables = listOf(
                        Table(primaryKey = Child::pk),
                        Table(primaryKey = Parent::pk),
                        Table(primaryKey = Complex::pk),
                        Table(primaryKey = Inherited::pk)
                    )
                )
            ),
            globalOutputs = listOf(),
            globalInputs = listOf(Output::class),
            globalSerializers = AllTS.basic,
            globalProcedures = listOf(ProcedureNotEmpty::class, ProcedureEmpty::class)
        )
    }

    @Test
    fun `test getKProps()`() {
        val kprops0 = this.mapper.getKProps(kclass = Parent::class)
        assertEquals(actual = kprops0, expected = listOf(Parent::col, Parent::pk))

        val kprops1 = this.mapper.getKProps(kclass = Complex::class)
        assertEquals(actual = kprops1, expected = listOf(Complex::pk, Complex::realName, Complex::realUsername))

        val kprops2 = this.mapper.getKProps(kclass = Inherited::class)
        assertEquals(actual = kprops2, expected = listOf(Inherited::pk, Inherited::test, Inherited::realName, Inherited::realUsername))

        val kprops3 = this.mapper.getKProps(kclass = ProcedureNotEmpty::class)
        assertEquals(actual = kprops3, expected = listOf(ProcedureNotEmpty::col, ProcedureNotEmpty::pk))

        val kprops4 = this.mapper.getKProps(kclass = ProcedureEmpty::class)
        assertEquals(actual = kprops4, expected = listOf())

        val kprops6 = this.mapper.getKProps(kclass = Output::class)
        assertEquals(actual = kprops6, expected = listOf(Output::col, Output::pk))

        val e1 = assertThrows<MapperException> {
            this.mapper.getKProps(kclass = String::class)
        }
        assertContains(charSequence = e1.message.toString(), other = "Could not find properties of class 'String'", message = e1.toString())

        val e2 = assertThrows<MapperException> {
            this.mapper.getKProps(kclass = Input::class)
        }
        assertContains(charSequence = e2.message.toString(), other = "Could not find properties of class 'Input'", message = e2.toString())
    }

    @Test
    fun `test getSerializer()`() {
        val ser0 = this.mapper.getSerializer(kprop = Parent::pk)
        assertEquals(actual = ser0, expected = NumberTS.Int)

        val ser1 = this.mapper.getSerializer(kprop = Output::pk)
        assertEquals(actual = ser1, expected = NumberTS.Int)

        val ser2 = this.mapper.getSerializer(kprop = ProcedureNotEmpty::col)
        assertEquals(actual = ser2, expected = StringTS.String(0))

        val e0 = assertThrows<MapperException> {
            this.mapper.getSerializer(kprop = String::length)
        }
        assertContains(
            charSequence = e0.message.toString(),
            "Could not find serializer of property: 'val kotlin.String.length: kotlin.Int'",
            message = e0.toString()
        )

        val e1 = assertThrows<MapperException> {
            this.mapper.getSerializer(kprop = Input::pk)
        }
        assertContains(
            charSequence = e1.message.toString(),
            "Could not find serializer of property: 'val com.urosjarc.dbmessiah.Test_DbMessiahMapper.Input.pk: kotlin.Int'",
            message = e1.toString()
        )
    }

    @Test
    fun `test getConstructor()`() {
        val conn0: KFunction<Any> = this.mapper.getConstructor(kclass = Parent::class)
        assertEquals(actual = conn0, expected = Parent::class.primaryConstructor as KFunction<Any>)

        val conn1: KFunction<Any> = this.mapper.getConstructor(kclass = Output::class)
        assertEquals(actual = conn1, expected = Output::class.primaryConstructor as KFunction<Any>)

        val conn2: KFunction<Any> = this.mapper.getConstructor(kclass = ProcedureNotEmpty::class)
        assertEquals(actual = conn2, expected = ProcedureNotEmpty::class.primaryConstructor as KFunction<Any>)

        val e0 = assertThrows<MapperException> {
            this.mapper.getConstructor(kclass = String::class)
        }
        assertContains(charSequence = e0.message.toString(), "Could not find primary constructor of kclass 'String'", message = e0.toString())

        val e1 = assertThrows<MapperException> {
            this.mapper.getConstructor(kclass = Input::class)
        }
        assertContains(charSequence = e1.message.toString(), "Could not find primary constructor of kclass 'Input'", message = e1.toString())
    }

    @Test
    fun `test createAssociationMaps()`() {

        val e0 = assertThrows<SerializerException> {
            this.mapper = Mapper(
                schemas = listOf(Schema(name = "main", tables = listOf(Table(primaryKey = Empty::pk)))),
                globalOutputs = listOf(),
                globalInputs = listOf(),
                globalSerializers = AllTS.basic,
                globalProcedures = listOf()
            )
        }
        assertContains(
            charSequence = e0.message.toString(),
            "Table 'Empty' have empty primary constructor, which is not allowed!",
            message = e0.toString()
        )

        val e1 = assertThrows<SerializerException> {
            this.mapper = Mapper(
                schemas = listOf(Schema(name = "main", tables = listOf(Table(primaryKey = Exotic::pk)))),
                globalOutputs = listOf(),
                globalInputs = listOf(),
                globalSerializers = AllTS.basic,
                globalProcedures = listOf()
            )
        }
        assertContains(
            charSequence = e1.message.toString(),
            "Could not find serializer for primary constructor parameter 'Exotic'.'localDate'",
            message = e1.toString()
        )

    }

    @Test
    fun `test getProcedure()`() {
        val ti0 = this.mapper.getProcedure(kclass = ProcedureNotEmpty::class)
        assertEquals(
            actual = ti0, expected = Procedure(
                schema = "main",
                kclass = ProcedureNotEmpty::class,
                args = listOf(
                    ProcedureArg(
                        kprop = ProcedureNotEmpty::col as KProperty1<Any, Any?>,
                        dbType = "VARCHAR",
                        jdbcType = JDBCType.VARCHAR,
                        encoder = StringTS.String(0).encoder,
                        decoder = StringTS.String(0).decoder
                    ),
                    ProcedureArg(
                        kprop = ProcedureNotEmpty::pk as KProperty1<Any, Any?>,
                        dbType = "INT",
                        jdbcType = JDBCType.INTEGER,
                        encoder = NumberTS.Int.encoder,
                        decoder = NumberTS.Int.decoder
                    )
                )
            )
        )

        val ti1 = this.mapper.getProcedure(kclass = ProcedureEmpty::class)
        assertEquals(
            actual = ti1, expected = Procedure(
                schema = "main",
                kclass = ProcedureEmpty::class,
                args = listOf()
            )
        )

        val e = assertThrows<SerializerException> {
            this.mapper.getProcedure(kclass = ProcedureNotRegistered::class)
        }
        assertContains(charSequence = e.message.toString(), other = " Could not find procedure for kclass: 'ProcedureNotRegistered'", message = e.toString())
    }

    @Test
    fun `test getTableInfo()`() {
        val ti0 = this.mapper.getTableInfo(obj = Parent(pk = 1, col = "asdf"))
        assertEquals(
            actual = ti0, expected = TableInfo(
                schema = "main",
                kclass = Parent::class,
                primaryKey = PrimaryColumn(
                    autoIncrement = true,
                    kprop = Parent::pk as KMutableProperty1<Any, Any?>,
                    dbType = "INT",
                    jdbcType = JDBCType.INTEGER,
                    encoder = NumberTS.Int.encoder,
                    decoder = NumberTS.Int.decoder
                ),
                foreignKeys = listOf(),
                otherColumns = listOf(),
                serializers = listOf()
            )
        )

        val ti1 = this.mapper.getTableInfo(kclass = Parent::class)
        assertEquals(
            actual = ti1, expected = TableInfo(
                schema = "main",
                kclass = Parent::class,
                primaryKey = PrimaryColumn(
                    autoIncrement = true,
                    kprop = Parent::pk as KMutableProperty1<Any, Any?>,
                    dbType = "INT",
                    jdbcType = JDBCType.INTEGER,
                    encoder = NumberTS.Int.encoder,
                    decoder = NumberTS.Int.decoder
                ),
                foreignKeys = listOf(),
                otherColumns = listOf(),
                serializers = listOf()
            )
        )
        val e = assertThrows<SerializerException> {
            this.mapper.getTableInfo(kclass = String::class)
        }
        assertContains(charSequence = e.message.toString(), other = "Could not find table info for table: 'String'", message = e.toString())
    }
}