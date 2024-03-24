package com.urosjarc.dbmessiah.serializers

import com.urosjarc.dbmessiah.data.TypeSerializer
import com.urosjarc.dbmessiah.serializers.BasicTS.basic

/**
 * Represents a collection of all TypeSerializers provided by the library.
 *
 * @property basic The basic kotlin TypeSerializers.
 */
public object BasicTS {

    public val basic: List<TypeSerializer<out Any>> = listOf(
        BooleanTS.all,
        CharTS.all,
        DecimalTS.all,
        NumberTS.all,
        StringTS.all,
        UNumber.all
    ).flatten()

    public val sqlite: List<TypeSerializer<out Any>> = basic + listOf(UUIDTS.sqlite)
    public val postgresql: List<TypeSerializer<out Any>> = basic + listOf(UUIDTS.postgresql)
    public val oracle: List<TypeSerializer<out Any>> = basic + listOf(UUIDTS.oracle)
    public val mysql: List<TypeSerializer<out Any>> = basic + listOf(UUIDTS.mysql)
    public val mssql: List<TypeSerializer<out Any>> = basic + listOf(UUIDTS.mssql)
    public val maria: List<TypeSerializer<out Any>> = basic + listOf(UUIDTS.maria)
    public val h2: List<TypeSerializer<out Any>> = basic + listOf(UUIDTS.h2)
    public val derby: List<TypeSerializer<out Any>> = basic + listOf(UUIDTS.derby)
    public val db2: List<TypeSerializer<out Any>> = basic + listOf(UUIDTS.db2)

}
