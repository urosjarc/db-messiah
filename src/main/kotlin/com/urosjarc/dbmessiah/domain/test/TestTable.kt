package com.urosjarc.dbmessiah.domain.test

data class TestTable(
    var id: Int? = null,
    var parent_id: Int? = null,

    //Integers
    val col0: Byte = 0,
    val col1: Short = 1,
    val col2: Int = 2,
    val col3: Long = 3,

    //Floats
    val col4: Float = 4.0f,
    val col5: Double = 5.0,

    //Unsigned
    val col6: UByte = 6u,
    val col7: UShort = 7u,
    val col8: UInt = 8U,
    val col9: ULong = 9u,

    //Bolean
    val col10: Boolean = true,

    //Char
    val col11: Char = 'a',

    //String
    var col12: String = "12"
)
