package domain

import Id

data class Album(
    var id: Id<Album>? = null,
    val title: String,
    val artistId: Id<Artist>
)
