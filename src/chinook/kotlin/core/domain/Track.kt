package core.domain

data class Track(
    val trackId: Id<Track>? = null,
    val name: String,
    val albumId: Id<Album>,
    val mediaTypeId: Id<MediaType>,
    val genreId: Id<Genre>,
    val composer: String,
    val miliseconds: Int,
    val bytes: Int,
    val unitPrice: Float
)
