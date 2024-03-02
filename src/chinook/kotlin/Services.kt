import com.urosjarc.dbmessiah.impl.db2.Db2Service
import com.urosjarc.dbmessiah.impl.derby.DerbyService
import com.urosjarc.dbmessiah.impl.h2.H2Service
import com.urosjarc.dbmessiah.impl.maria.MariaService
import com.urosjarc.dbmessiah.impl.mssql.MssqlService
import com.urosjarc.dbmessiah.impl.mysql.MysqlService
import com.urosjarc.dbmessiah.impl.oracle.OracleService
import com.urosjarc.dbmessiah.impl.postgresql.PgService
import com.urosjarc.dbmessiah.impl.sqlite.SqliteService
import java.util.*

/**
 * Lets create database services with jdbcUrl as defined in docker compose file.
 */
val h2 = H2Service(
    config = Properties().apply {
        this["jdbcUrl"] = "jdbc:h2:mem:main"
    },
    ser = h2_serializer
)
val derby = DerbyService(
    config = Properties().apply {
        this["jdbcUrl"] = "jdbc:derby:memory:db;create=true"
    },
    ser = derby_serializer
)
val db2 = Db2Service(
    config = Properties().apply {
        this["jdbcUrl"] = "jdbc:db2://localhost:50000/main"
        this["username"] = "db2inst1"
        this["password"] = "root"
    },
    ser = db2_serializer
)
val maria = MariaService(
    config = Properties().apply {
        this["jdbcUrl"] = "jdbc:mariadb://localhost:3306"
        this["username"] = "root"
        this["password"] = "root"
    },
    ser = maria_serializer
)
val mssql = MssqlService(
    config = Properties().apply {
        this["jdbcUrl"] = "jdbc:sqlserver://localhost:1433;encrypt=false;"
        this["username"] = "sa"
        this["password"] = "Root_root1"
    },
    ser = mssql_serializer
)
val mysql = MysqlService(
    config = Properties().apply {
        this["jdbcUrl"] = "jdbc:mysql://localhost:3307"
        this["username"] = "root"
        this["password"] = "root"
    },
    ser = mysql_serializer
)
val oracle = OracleService(
    config = Properties().apply {
        this["jdbcUrl"] = "jdbc:oracle:thin:@localhost:1521:XE"
        this["username"] = "system"
        this["password"] = "root"
    },
    ser = oracle_serializer
)
val pg = PgService(
    config = Properties().apply {
        this["jdbcUrl"] = "jdbc:postgresql://localhost:5432/public"
        this["username"] = "root"
        this["password"] = "root"
    },
    ser = postgresql_serializer
)

val sqlite = SqliteService(
    config = Properties().apply {
        this["jdbcUrl"] = "jdbc:sqlite::memory:"
    },
    ser = sqlite_serializer
)
