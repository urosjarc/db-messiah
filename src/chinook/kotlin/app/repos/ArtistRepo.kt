package app.repos

import app.services.PostgresService
import app.services.SqlitService
import core.domain.Artist
import core.domain.Id
import core.repos.ArtistRepoContract


class ArtistRepo(
    sqliteService: SqlitService,
    postgresService: PostgresService
) : ArtistRepoContract {

    val sqlite = sqliteService.db
    val pg = postgresService.db
    override fun getArtist(pk: Id<Artist>): Artist {
        /**
         * Inputs
         */
        // var artist: Artist? = null

        /**
         * Actions
         */
        // sqlite.autocommit {
        //     artist = it.row.select(Artist::class, pk = pk.value)
        // }

        /**
         * Outputs
         */
        // return artist!!
        return Artist(name = "asdf")
    }
}
