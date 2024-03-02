package domain

import Id

data class PlaylistTrack(
    var id: Id<PlaylistTrack>? = null,
    val playlistId: Id<Playlist>,
    val trackId: Id<Track>
)
