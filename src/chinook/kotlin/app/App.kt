package app

import app.repos.ArtistRepo
import app.repos.CustomerRepo
import app.services.EmailService
import app.services.PostgresService
import app.services.SqlitService
import core.use_cases.Login
import core.use_cases.SendNotifications
import core.use_cases.SignIn
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module

object App {
    fun modul() = module {
        /**
         * Services
         */
        this.single<SqlitService> {
            SqlitService(
                username = Env.sqlite.USERNAME,
                password = Env.sqlite.PASSWORD,
                jdbcUrl = Env.sqlite.JDBCURL
            )
        }
        this.single<PostgresService> {
            PostgresService(
                username = Env.postgres.USERNAME,
                password = Env.postgres.PASSWORD,
                jdbcUrl = Env.postgres.JDBCURL
            )
        }
        this.single<EmailService> {
            EmailService(
                token = Env.email.TOKEN,
            )
        }

        /**
         * Repos
         */
        this.single { ArtistRepo(get(), get()) }
        this.single { CustomerRepo(get(), get()) }

        /**
         * Usecases
         */
        this.factory { SignIn(get(), get(), get()) }
        this.factory { Login(get(), get(), get()) }
        this.factory { SendNotifications(get(), get()) }
    }

    fun init() {
        startKoin { this.modules(modul()) }
    }

    fun reset() = stopKoin()
}
