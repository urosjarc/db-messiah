package domain

import Id

data class Artist(
    var id: Id<Artist>? = null,
    val name: String
)
