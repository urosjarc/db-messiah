package com.urosjarc.dbmessiah.serializers

import com.urosjarc.dbmessiah.data.TypeSerializer

public object JavaTimeTS {
    private val DATETIME = listOf(InstantTS.DATETIME, LocalDateTS.DATE, LocalTimeTS.TIME)
    private val TIMESTAMP = listOf(InstantTS.TIMESTAMP, LocalDateTS.DATE, LocalTimeTS.TIME)

    public val sqlite: List<TypeSerializer<out Any>> = DATETIME
    public val postgresql: List<TypeSerializer<out Any>> = TIMESTAMP
    public val oracle: List<TypeSerializer<out Any>> = listOf(InstantTS.TIMESTAMP, LocalDateTS.DATE, LocalTimeTS.NUMBER5)
    public val mysql: List<TypeSerializer<out Any>> = DATETIME
    public val mssql: List<TypeSerializer<out Any>> = DATETIME
    public val maria: List<TypeSerializer<out Any>> = DATETIME
    public val h2: List<TypeSerializer<out Any>> = DATETIME
    public val derby: List<TypeSerializer<out Any>> = TIMESTAMP
    public val db2: List<TypeSerializer<out Any>> = DATETIME
}
