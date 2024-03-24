package com.urosjarc.dbmessiah.builders

import com.urosjarc.dbmessiah.domain.Table
import com.urosjarc.dbmessiah.impl.sqlite.SqliteSerializer
import com.urosjarc.dbmessiah.serializers.BasicTS
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class Test_ProcedureBuilder {
    private lateinit var ser: SqliteSerializer
    private lateinit var pb: ProcedureBuilder<Procedure>

    data class Procedure(val col: Int)
    data class Parent(var col: Int?)

    @BeforeEach
    fun init() {
        this.ser = SqliteSerializer(
            tables = listOf(
                Table(Parent::col)
            ),
            globalSerializers = BasicTS.basic
        )
        this.pb = ProcedureBuilder(ser = ser, procedure = Procedure::class)
    }

    @Test
    fun `test arg`() {
        assertEquals(expected = "\"${Procedure::col.name}\"", actual = this.pb.arg(Procedure::col))
    }

}
