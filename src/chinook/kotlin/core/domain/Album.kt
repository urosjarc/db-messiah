package core.domain

data class Album(
    val albumId: Id<Album>? = null,
    val title: String,
    val artistId: Id<Artist>
)
