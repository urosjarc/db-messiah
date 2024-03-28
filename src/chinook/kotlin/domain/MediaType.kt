package domain

import Id

data class MediaType(
    val id: Id<MediaType> = Id(),
    val name: String
)
