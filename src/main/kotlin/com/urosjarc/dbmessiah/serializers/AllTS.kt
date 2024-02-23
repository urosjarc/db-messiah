package com.urosjarc.dbmessiah.serializers

import com.urosjarc.dbmessiah.data.TypeSerializer

/**
 * Represents a collection of all TypeSerializers provided by the library.
 *
 * @property basic The basic kotlin TypeSerializers.
 */
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
