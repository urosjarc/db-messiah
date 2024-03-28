package domain

import Id

data class Artist(
    val id: Id<Artist> = Id(),
    val name: String
)
