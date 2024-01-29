import com.urosjarc.dbmessiah.domain.serialization.TypeSerializer
import java.sql.JDBCType

object BooleanTS {
    val Boolean = TypeSerializer(
        kclass = Boolean::class,
        dbType = "BOOL",
        jdbcType = JDBCType.BOOLEAN,
        decoder = { rs, i, dInfo -> rs.getBoolean(i) },
        encoder = { ps, i, x -> ps.setBoolean(i, x) })

    val all = listOf(
        Boolean
    )
}