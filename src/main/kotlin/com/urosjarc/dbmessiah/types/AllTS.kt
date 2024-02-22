package com.urosjarc.dbmessiah.types

import com.urosjarc.dbmessiah.domain.serialization.TypeSerializer

public object AllTS {

    public val basic: List<TypeSerializer<out Any>> = listOf(
        BooleanTS.all,
        CharTS.all,
        FloatTS.all,
        NumberTS.all,
        StringTS.all,
        UNumber.all
    ).flatten()

}
