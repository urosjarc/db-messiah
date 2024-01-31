import com.urosjarc.dbmessiah.domain.serialization.TypeSerializer
import java.sql.JDBCType

object NumberTS {
    val BIT = TypeSerializer(
        kclass = Boolean::class,
        dbType = "BIT",
        jdbcType = JDBCType.BIT,
        decoder = { rs, i, _ -> rs.getBoolean(i) },
        encoder = { ps, i, x -> ps.setBoolean(i, x) })

    val Byte = TypeSerializer(
        kclass = Byte::class,
        dbType = "TINYINT",
        jdbcType = JDBCType.TINYINT,
        decoder = { rs, i, _ -> rs.getByte(i) },
        encoder = { ps, i, x -> ps.setByte(i, x) })

    val Short = TypeSerializer(
        kclass = Short::class,
        dbType = "SMALLINT",
        jdbcType = JDBCType.SMALLINT,
        decoder = { rs, i, _ -> rs.getShort(i) },
        encoder = { ps, i, x -> ps.setShort(i, x) })

    val Int = TypeSerializer(
        kclass = Int::class,
        dbType = "INTEGER",
        jdbcType = JDBCType.INTEGER,
        decoder = { rs, i, _ -> rs.getInt(i) },
        encoder = { ps, i, x -> ps.setInt(i, x) })

    val Long = TypeSerializer(
        kclass = Long::class,
        dbType = "BIGINT",
        jdbcType = JDBCType.BIGINT,
        decoder = { rs, i, _ -> rs.getLong(i) },
        encoder = { ps, i, x -> ps.setLong(i, x) })

    val all = listOf(
        BIT,
        Byte,
        Short,
        Int,
        Long
    )
}
