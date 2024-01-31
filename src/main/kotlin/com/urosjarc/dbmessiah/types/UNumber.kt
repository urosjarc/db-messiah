import com.urosjarc.dbmessiah.domain.serialization.TypeSerializer
import java.sql.JDBCType

object UNumber {
    val UByte = TypeSerializer(
        kclass = UByte::class,
        dbType = "TINYINT",
        jdbcType = JDBCType.TINYINT,
        decoder = { rs, i, _ -> rs.getByte(i).toUByte() },
        encoder = { ps, i, x -> ps.setByte(i, x.toByte()) })

    val UShort = TypeSerializer(
        kclass = UShort::class,
        dbType = "SMALLINT",
        jdbcType = JDBCType.SMALLINT,
        decoder = { rs, i, _ -> rs.getShort(i).toUShort() },
        encoder = { ps, i, x -> ps.setShort(i, x.toShort()) })

    val UInt = TypeSerializer(
        kclass = UInt::class,
        dbType = "INTEGER",
        jdbcType = JDBCType.INTEGER,
        decoder = { rs, i, _ -> rs.getInt(i).toUInt() },
        encoder = { ps, i, x -> ps.setInt(i, x.toInt()) })

    val ULong = TypeSerializer(
        kclass = ULong::class,
        dbType = "BIGINT",
        jdbcType = JDBCType.BIGINT,
        decoder = { rs, i, _ -> rs.getLong(i).toULong() },
        encoder = { ps, i, x -> ps.setLong(i, x.toLong()) })

    val all = listOf(
        UByte,
        UShort,
        UInt,
        ULong
    )

}
