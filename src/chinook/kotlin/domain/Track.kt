package domain

import Id

data class Track(
    val id: Id<Track> = Id(),
    val name: String,
    val albumId: Id<Album>,
    val mediaTypeId: Id<MediaType>,
    val genreId: Id<Genre>,
    val composer: String,
//    val miliseconds: Int,
//    val bytes: Int,
//    val unitPrice: Float
)
