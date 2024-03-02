package domain

import Id

data class MediaType(
    var id: Id<MediaType>? = null,
    val name: String
)
