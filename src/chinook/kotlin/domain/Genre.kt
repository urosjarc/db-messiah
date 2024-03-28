package domain

import Id

data class Genre(
    val id: Id<Genre> = Id(),
    val name: String
)
