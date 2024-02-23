package com.urosjarc.dbmessiah.serializers

import com.urosjarc.dbmessiah.data.TypeSerializer

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
