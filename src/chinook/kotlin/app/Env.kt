package app

object Env {
    object sqlite {
        val USERNAME: String = System.getenv("SQLITE_USERNAME") ?: "root"
        val PASSWORD: String = System.getenv("SQLITE_PASSWORD") ?: "root"
        val JDBCURL: String = System.getenv("SQLITE_JDBCURL") ?: "jdbc:sqlite::memory:"
    }

    object postgres {
        val USERNAME: String = System.getenv("POSTGRES_USERNAME") ?: "root"
        val PASSWORD: String = System.getenv("POSTGRES_PASSWORD") ?: "root"
        val JDBCURL: String = System.getenv("POSTGRES_JDBCURL") ?: "jdbc:postgresql://localhost:5432/public"
    }
    object email {
        val TOKEN: String = System.getenv("EMAIL_TOKEN") ?: "some super secret token"
    }
}
