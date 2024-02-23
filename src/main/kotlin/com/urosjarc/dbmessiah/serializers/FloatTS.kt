package com.urosjarc.dbmessiah.serializers
import com.urosjarc.dbmessiah.data.TypeSerializer
import java.sql.JDBCType

public object FloatTS {

    public val float: TypeSerializer<Float> = TypeSerializer(
        kclass = Float::class,
        dbType = "FLOAT",
        jdbcType = JDBCType.FLOAT,
        decoder = { rs, i, _ -> rs.getFloat(i) },
        encoder = { ps, i, x -> ps.setFloat(i, x) })

    public val double: TypeSerializer<Double> = TypeSerializer(
        kclass = Double::class,
        dbType = "DOUBLE",
        jdbcType = JDBCType.DOUBLE,
        decoder = { rs, i, _ -> rs.getDouble(i) },
        encoder = { ps, i, x -> ps.setDouble(i, x) })

    public val all: List<TypeSerializer<out Any>> = listOf(
        float,
        double
    )
}
