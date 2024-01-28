package com.urosjarc.dbmessiah.types

import BooleanTS
import FloatTS
import NumberTS
import UNumber

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
