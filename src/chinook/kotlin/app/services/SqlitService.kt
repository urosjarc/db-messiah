package app.services

import app.data.sqlite_serializer
import java.util.*
import com.urosjarc.dbmessiah.impl.sqlite.SqliteService as SqliteSer

class SqlitService(val username: String, val password: String, val jdbcUrl: String) {

    val db = SqliteSer(
        config = Properties().apply {
            this["jdbcUrl"] = jdbcUrl
            this["username"] = username
            this["password"] = password
        },
        ser = sqlite_serializer
    )

}
