package core.repos

import core.domain.Artist
import core.domain.Id

interface ArtistRepoContract {
    fun getArtist(pk: Id<Artist>): Artist
}
