package app.services

import app.data.postgresql_serializer
import com.urosjarc.dbmessiah.impl.postgresql.PgService
import java.util.*

class PostgresService(val username: String, val password: String, val jdbcUrl: String) {

    val db = PgService(
        config = Properties().apply {
            this["jdbcUrl"] = jdbcUrl
            this["username"] = username
            this["password"] = password
        },
        ser = postgresql_serializer
    )

}
