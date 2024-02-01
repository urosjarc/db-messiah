package com.urosjarc.dbmessiah.types

object AllTS {

    val basic = listOf(
        BooleanTS.all,
        CharTS.all,
        FloatTS.all,
        NumberTS.all,
        StringTS.all,
        UNumber.all
    ).flatten()

}
