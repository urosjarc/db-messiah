package com.urosjarc.dbmessiah.builders

import com.urosjarc.dbmessiah.domain.Table
import com.urosjarc.dbmessiah.exceptions.QueryException
import com.urosjarc.dbmessiah.impl.maria.MariaSchema
import com.urosjarc.dbmessiah.impl.maria.MariaSerializer
import com.urosjarc.dbmessiah.serializers.AllTS
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertContains
import kotlin.test.assertEquals

class Test_SqlBuilder {
    private lateinit var ser: MariaSerializer
    private lateinit var sb: SqlBuilder

    data class Parent(var col0: Int?, val col1: String, val col2: Float)
    data class Procedure(var col0: Int?)

    @BeforeEach
    fun init() {
        this.ser = MariaSerializer(
            schemas = listOf(MariaSchema(name = "main", tables = listOf(Table(Parent::col0)))),
            globalSerializers = AllTS.basic,
            globalProcedures = listOf(Procedure::class)
        )

        this.sb = SqlBuilder(ser = ser)
    }

    @Test
    fun `test SELECT`() {
        assertEquals(actual = this.sb.SELECT<Parent>(), expected = "SELECT * FROM `main`.`Parent`")
    }

    @Test
    fun `test DELETE`() {
        assertEquals(actual = this.sb.DELETE<Parent>(), expected = "DELETE FROM `main`.`Parent`")
    }

    @Test
    fun `test table`() {
        assertEquals(actual = this.sb.table<Parent>(), expected = "`main`.`Parent`")
    }

    @Test
    fun `test column`() {
        assertEquals(actual = this.sb.column(Parent::col0), expected = "`main`.`Parent`.`col0`")
    }

    @Test
    fun `test name`() {
        assertEquals(actual = this.sb.name(Parent::col0), expected = "`col0`")
    }

    @Test
    fun `test procedure`() {
        assertEquals(actual = this.sb.procedure<Procedure>(), expected = "`Procedure`")
    }
}
