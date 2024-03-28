package domain

import Id

data class PlaylistTrack(
    val id: Id<PlaylistTrack> = Id(),
    val playlistId: Id<Playlist>,
    val trackId: Id<Track>
)
