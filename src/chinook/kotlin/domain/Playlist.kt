package domain

import Id

data class Playlist(
    val id: Id<Playlist> = Id(),
    val name: String
)
