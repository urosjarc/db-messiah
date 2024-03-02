package domain

import Id

data class Playlist(
    var id: Id<Playlist>? = null,
    val name: String
)
