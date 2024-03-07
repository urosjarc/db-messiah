package com.urosjarc.dbmessiah.serializers

import com.urosjarc.dbmessiah.data.TypeSerializer
import com.urosjarc.dbmessiah.serializers.AllTS.basic

/**
 * Represents a collection of all TypeSerializers provided by the library.
 *
 * @property basic The basic kotlin TypeSerializers.
 */
public object AllTS {

    public val basic: List<TypeSerializer<out Any>> = listOf(
        BooleanTS.all,
        CharTS.all,
        DecimalTS.all,
        NumberTS.all,
        StringTS.all,
        UNumber.all
    ).flatten()

    public val sqlite: List<TypeSerializer<out Any>> = listOf(UUIDTS.sqlite) + basic
    public val postgresql: List<TypeSerializer<out Any>> = listOf(UUIDTS.postgresql) + basic
    public val oracle: List<TypeSerializer<out Any>> = listOf(UUIDTS.oracle) + basic
    public val mysql: List<TypeSerializer<out Any>> = listOf(UUIDTS.mysql) + basic
    public val mssql: List<TypeSerializer<out Any>> = listOf(UUIDTS.mssql) + basic
    public val maria: List<TypeSerializer<out Any>> = listOf(UUIDTS.maria) + basic
    public val h2: List<TypeSerializer<out Any>> = listOf(UUIDTS.h2) + basic
    public val derby: List<TypeSerializer<out Any>> = listOf(UUIDTS.derby) + basic
    public val db2: List<TypeSerializer<out Any>> = listOf(UUIDTS.db2) + basic

}
