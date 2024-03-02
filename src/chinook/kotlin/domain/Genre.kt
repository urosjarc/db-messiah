package domain

import Id

data class Genre(
    var id: Id<Genre>? = null,
    val name: String
)
