package com.urosjarc.dbmessiah.types
import com.urosjarc.dbmessiah.domain.serialization.TypeSerializer
import java.sql.JDBCType

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
