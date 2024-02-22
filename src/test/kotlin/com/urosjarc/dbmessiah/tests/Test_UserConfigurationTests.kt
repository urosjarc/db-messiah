package com.urosjarc.dbmessiah.tests

import com.urosjarc.dbmessiah.Mapper
import com.urosjarc.dbmessiah.Schema
import com.urosjarc.dbmessiah.domain.columns.C
import com.urosjarc.dbmessiah.domain.table.Table
import com.urosjarc.dbmessiah.exceptions.SerializerException
import com.urosjarc.dbmessiah.types.AllTS
import com.urosjarc.dbmessiah.types.NumberTS
import org.junit.jupiter.api.assertThrows
import kotlin.test.Test
import kotlin.test.assertContains

class Test_UserConfigurationTests {

    private data class Child(var pk: Int, val fk: String, val col: Float)
    private data class Parent(var pk: String, val col: String)
    private data class Unknown(var pk: String? = null)

    @Test
    fun `test 1-th()`() {
        val e = assertThrows<SerializerException> {
            UserConfigurationTests(
                mapper = Mapper(
                    schemas = listOf(), globalSerializers = listOf(),
                    globalOutputs = listOf(), globalInputs = listOf(),
                    globalProcedures = listOf()
                )
            )
        }

        assertContains(charSequence = e.message.toString(), other = "Missing schema", message = e.toString())

        val e2 = assertThrows<SerializerException> {
            UserConfigurationTests(
                mapper = Mapper(
                    schemas = listOf(Schema(name = "Schema0", tables = listOf())), globalSerializers = listOf(),
                    globalOutputs = listOf(), globalInputs = listOf(),
                    globalProcedures = listOf()
                )
            )
        }

        assertContains(charSequence = e2.message.toString(), other = "no registered table", message = e2.toString())
    }

    @Test
    fun `test 2-th()`() {
        val e = assertThrows<SerializerException> {
            UserConfigurationTests(
                mapper = Mapper(
                    schemas = listOf(
                        Schema(name = "Schema0", tables = listOf(Table(Parent::pk))),
                        Schema(name = "Schema0", tables = listOf(Table(Parent::pk))),

                        ), globalSerializers = AllTS.basic,
                    globalOutputs = listOf(), globalInputs = listOf(),
                    globalProcedures = listOf()
                )
            )
        }

        assertContains(charSequence = e.message.toString(), other = "Schemas registered multiple times: ['Schema0']", message = e.toString())
    }

    @Test
    fun `test 3-th()`() {
        val e = assertThrows<SerializerException> {
            UserConfigurationTests(
                mapper = Mapper(
                    schemas = listOf(
                        Schema(
                            name = "Schema0", tables = listOf(
                                Table(Parent::pk),
                                Table(Parent::pk)
                            )
                        ),
                    ), globalSerializers = AllTS.basic,
                    globalOutputs = listOf(), globalInputs = listOf(),
                    globalProcedures = listOf()
                )
            )
        }
        assertContains(
            charSequence = e.message.toString(),
            other = "Schema 'Schema0' has tables ['Parent'] registered multiple times",
            message = e.toString()
        )
    }

    @Test
    fun `test 4-th()`() {
        val e = assertThrows<SerializerException> {
            UserConfigurationTests(
                mapper = Mapper(
                    schemas = listOf(
                        Schema(
                            name = "Schema0", tables = listOf(
                                Table(
                                    Parent::pk, foreignKeys = listOf(
                                        Parent::col to Child::class,
                                        Parent::col to Child::class
                                    )
                                ),
                            )
                        ),
                    ), globalSerializers = AllTS.basic,
                    globalOutputs = listOf(), globalInputs = listOf(),
                    globalProcedures = listOf()
                )
            )
        }
        assertContains(
            charSequence = e.message.toString(),
            other = "Table 'Schema0'.'Parent' has foreign keys ['col'] registered multiple times",
            message = e.toString()
        )
    }

    @Test
    fun `test 5-th()`() {
        val e = assertThrows<SerializerException> {
            UserConfigurationTests(
                mapper = Mapper(
                    schemas = listOf(
                        Schema(
                            name = "Schema0",
                            tables = listOf(
                                Table(
                                    Parent::pk, foreignKeys = listOf(
                                        Parent::col to Child::class,
                                    ),
                                    constraints = listOf(
                                        Parent::col to listOf(),
                                        Parent::col to listOf()
                                    )
                                ),
                            ),
                        ),
                    ), globalSerializers = AllTS.basic,
                    globalOutputs = listOf(), globalInputs = listOf(),
                    globalProcedures = listOf()
                )
            )
        }
        assertContains(
            charSequence = e.message.toString(),
            other = "Table 'Schema0'.'Parent' has constraints ['col'] registered multiple times",
            message = e.toString()
        )
    }

    @Test
    fun `test 6-th()`() {
        val e = assertThrows<SerializerException> {
            UserConfigurationTests(
                mapper = Mapper(
                    schemas = listOf(
                        Schema(
                            name = "Schema0",
                            tables = listOf(
                                Table(Child::pk),
                            ),
                            serializers = listOf(NumberTS.Int) + listOf(NumberTS.Int),
                        ),
                    ), globalSerializers = AllTS.basic + NumberTS.Int,
                    globalOutputs = listOf(), globalInputs = listOf(),
                    globalProcedures = listOf()
                )
            )
        }
        assertContains(
            charSequence = e.message.toString(),
            other = "Schema 'Schema0' has serializers [TS<Int>] registered multiple times",
            message = e.toString()
        )
    }

    @Test
    fun `test 7-th()`() {
        val e = assertThrows<SerializerException> {
            UserConfigurationTests(
                mapper = Mapper(
                    schemas = listOf(
                        Schema(
                            name = "Schema0",
                            tables = listOf(
                                Table(
                                    primaryKey = Child::pk,
                                    serializers = listOf(NumberTS.Int) + listOf(NumberTS.Int),
                                ),
                            ),
                        ),
                    ), globalSerializers = AllTS.basic + NumberTS.Int,
                    globalOutputs = listOf(), globalInputs = listOf(),
                    globalProcedures = listOf()
                )
            )
        }
        assertContains(
            charSequence = e.message.toString(),
            other = "Table 'Schema0'.'Child' has serializers [TS<Int>] registered multiple times",
            message = e.toString()
        )
    }

    @Test
    fun `test 8-th()`() {
        val e = assertThrows<SerializerException> {
            UserConfigurationTests(
                mapper = Mapper(
                    schemas = listOf(
                        Schema(
                            name = "Schema0",
                            tables = listOf(
                                Table(
                                    primaryKey = Child::pk,
                                    foreignKeys = listOf(
                                        Child::col to Parent::class
                                    )
                                ),
                            ),
                        ),
                    ), globalSerializers = AllTS.basic,
                    globalOutputs = listOf(), globalInputs = listOf(),
                    globalProcedures = listOf()
                )
            )
        }
        assertContains(
            charSequence = e.message.toString(),
            other = " Foreign key 'Schema0'.'Child'.'col' points to unregistered class 'Parent'",
            message = e.toString()
        )

        val e2 = assertThrows<SerializerException> {
            UserConfigurationTests(
                mapper = Mapper(
                    schemas = listOf(
                        Schema(
                            name = "Schema0",
                            tables = listOf(
                                Table(primaryKey = Parent::pk),
                                Table(
                                    primaryKey = Child::pk,
                                    foreignKeys = listOf(
                                        Child::col to Parent::class
                                    )
                                ),
                            ),
                        ),
                    ), globalSerializers = AllTS.basic,
                    globalOutputs = listOf(), globalInputs = listOf(),
                    globalProcedures = listOf()
                )
            )
        }
        assertContains(
            charSequence = e2.message.toString(),
            other = "Foreign key 'Schema0'.'Child'.'col' is type of 'Float' but it points to incompatible primary key 'Schema0'.'Parent'.'pk'",
            message = e2.toString()
        )
    }

    @Test
    fun `test 9-th()`() {
        val e = assertThrows<SerializerException> {
            UserConfigurationTests(
                mapper = Mapper(
                    schemas = listOf(
                        Schema(
                            name = "Schema0",
                            tables = listOf(
                                Table(Parent::pk),
                                Table(
                                    primaryKey = Child::pk,
                                    foreignKeys = listOf(
                                        Child::fk to Parent::class
                                    ),
                                    constraints = listOf(
                                        Child::fk to listOf(C.AUTO_INC)
                                    )
                                ),
                            ),
                        ),
                    ), globalSerializers = AllTS.basic,
                    globalOutputs = listOf(), globalInputs = listOf(),
                    globalProcedures = listOf()
                )
            )
        }
        assertContains(
            charSequence = e.message.toString(),
            other = "Foreign key 'Schema0'.'Child'.'fk' does not need 'AUTO_INC' constraint",
            message = e.toString()
        )

        val e2 = assertThrows<SerializerException> {
            UserConfigurationTests(
                mapper = Mapper(
                    schemas = listOf(
                        Schema(
                            name = "Schema0",
                            tables = listOf(
                                Table(
                                    primaryKey = Child::pk,
                                    constraints = listOf(
                                        Child::pk to listOf(C.UNIQUE)
                                    )
                                ),
                            ),
                        ),
                    ), globalSerializers = AllTS.basic,
                    globalOutputs = listOf(), globalInputs = listOf(),
                    globalProcedures = listOf()
                )
            )
        }
        assertContains(
            charSequence = e2.message.toString(),
            other = "Primary key 'Schema0'.'Child'.'pk' does not need 'UNIQUE' constraint",
            message = e2.toString()
        )
    }

    @Test
    fun `test 10-th()`() {
        val e = assertThrows<SerializerException> {
            UserConfigurationTests(
                mapper = Mapper(
                    schemas = listOf(
                        Schema(name = "Schema0", tables = listOf(Table(primaryKey = Unknown::pk))),
                    ),
                    globalSerializers = AllTS.basic,
                    globalOutputs = listOf(Unknown::class),
                    globalInputs = listOf(),
                    globalProcedures = listOf()
                )
            )
        }
        assertContains(
            charSequence = e.message.toString(),
            other = "Primary key 'Schema0.Unknown.pk' of type 'String' has constrain 'AUTO_INC' but then it should be of type 'Int'",
            message = e.toString()
        )
    }

    @Test
    fun `test 11-th()`() {
        val e = assertThrows<SerializerException> {
            UserConfigurationTests(
                mapper = Mapper(
                    schemas = listOf(
                        Schema(name = "Schema0", tables = listOf(Table(primaryKey = Child::pk))),
                    ),
                    globalSerializers = AllTS.basic,
                    globalOutputs = listOf(),
                    globalInputs = listOf(Unknown::class),
                    globalProcedures = listOf()
                )
            )
        }
        assertContains(
            charSequence = e.message.toString(),
            other = "Input property 'Unknown'.'pk' can be null which is not allowed on any input class!",
            message = e.toString()
        )
    }

    @Test
    fun `test 12-th()`() {
        val e = assertThrows<SerializerException> {
            UserConfigurationTests(
                mapper = Mapper(
                    schemas = listOf(
                        Schema(name = "Schema0", tables = listOf(Table(primaryKey = Child::pk))),
                    ),
                    globalSerializers = AllTS.basic,
                    globalOutputs = listOf(Unknown::class),
                    globalInputs = listOf(),
                    globalProcedures = listOf()
                )
            )
        }
        assertContains(
            charSequence = e.message.toString(),
            other = "Output class 'Unknown' have primary constructor with optional arguments ['pk'], which is not allowed on any output class!"
        )
    }

    @Test
    fun `test 13-th()`() {
        val e = assertThrows<SerializerException> {
            UserConfigurationTests(
                mapper = Mapper(
                    schemas = listOf(
                        Schema(
                            name = "Schema0",
                            tables = listOf(
                                Table(Child::pk),
                            ),
                            serializers = listOf(NumberTS.Int, NumberTS.Int),
                        ),
                    ), globalSerializers = AllTS.basic,
                    globalOutputs = listOf(), globalInputs = listOf(),
                    globalProcedures = listOf(Parent::class)
                )
            )
        }
        assertContains(
            charSequence = e.message.toString(),
            other = "Schema 'Schema0' has serializers [TS<Int>] registered multiple times",
            message = e.toString()
        )
    }

    @Test
    fun `test 14-th()`() {
        val e = assertThrows<SerializerException> {
            UserConfigurationTests(
                mapper = Mapper(
                    schemas = listOf(
                        Schema(
                            name = "Schema0",
                            tables = listOf(
                                Table(Child::pk),
                            ),
                            serializers = listOf(NumberTS.Int, NumberTS.Int),
                        ),
                    ), globalSerializers = AllTS.basic + NumberTS.Int,
                    globalOutputs = listOf(Parent::class), globalInputs = listOf(),
                    globalProcedures = listOf()
                )
            )
        }
        assertContains(
            charSequence = e.message.toString(),
            other = "Schema 'Schema0' has serializers [TS<Int>] registered multiple times",
            message = e.toString()
        )
    }

    @Test
    fun `test 15-th()`() {
        val e = assertThrows<SerializerException> {
            UserConfigurationTests(
                mapper = Mapper(
                    schemas = listOf(
                        Schema(
                            name = "Schema0",
                            tables = listOf(
                                Table(Child::pk),
                            ),
                            serializers = listOf(NumberTS.Int, NumberTS.Int),
                        ),
                    ), globalSerializers = AllTS.basic + NumberTS.Int,
                    globalOutputs = listOf(), globalInputs = listOf(Parent::class, Parent::class),
                    globalProcedures = listOf()
                )
            )
        }
        assertContains(
            charSequence = e.message.toString(),
            other = "Schema 'Schema0' has serializers [TS<Int>] registered multiple times",
            message = e.toString()
        )
    }

    @Test
    fun `test 16-th()`() {
        val e = assertThrows<SerializerException> {
            UserConfigurationTests(
                mapper = Mapper(
                    schemas = listOf(
                        Schema(
                            name = "Schema0",
                            tables = listOf(
                                Table(Child::pk),
                            ),
                            serializers = listOf(NumberTS.Int, NumberTS.Int),
                        ),
                    ), globalSerializers = AllTS.basic + NumberTS.Int,
                    globalOutputs = listOf(), globalInputs = listOf(Parent::class, Parent::class),
                    globalProcedures = listOf()
                )
            )
        }
        assertContains(
            charSequence = e.message.toString(),
            other = "Schema 'Schema0' has serializers [TS<Int>] registered multiple times",
            message = e.toString()
        )
    }
}
