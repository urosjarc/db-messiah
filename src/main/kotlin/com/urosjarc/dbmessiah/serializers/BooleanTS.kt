package com.urosjarc.dbmessiah.serializers

import com.urosjarc.dbmessiah.data.TypeSerializer
import java.sql.JDBCType

/**
 * BooleanTS class represents a collection of TypeSerializers for the Boolean types.
 */
public object BooleanTS {

    public val boolean: TypeSerializer<Boolean> = TypeSerializer(
        kclass = Boolean::class,
        dbType = "BOOL",
        jdbcType = JDBCType.BOOLEAN,
        decoder = { rs, i, _ -> rs.getBoolean(i) },
        encoder = { ps, i, x -> ps.setBoolean(i, x) })

    public val all: List<TypeSerializer<Boolean>> = listOf(
        boolean
    )
}
