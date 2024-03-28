package domain

import Id

data class Album(
    val id: Id<Album> = Id(),
    val title: String,
    val artistId: Id<Artist>
)
