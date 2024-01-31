import com.urosjarc.dbmessiah.domain.serialization.TypeSerializer
import java.sql.JDBCType

object FloatTS {

    val Float = TypeSerializer(
        kclass = Float::class,
        dbType = "FLOAT",
        jdbcType = JDBCType.FLOAT,
        decoder = { rs, i, _ -> rs.getFloat(i) },
        encoder = { ps, i, x -> ps.setFloat(i, x) })

    val Double = TypeSerializer(
        kclass = Double::class,
        dbType = "DOUBLE",
        jdbcType = JDBCType.DOUBLE,
        decoder = { rs, i, _ -> rs.getDouble(i) },
        encoder = { ps, i, x -> ps.setDouble(i, x) })

    val all = listOf(
        Float,
        Double
    )
}
