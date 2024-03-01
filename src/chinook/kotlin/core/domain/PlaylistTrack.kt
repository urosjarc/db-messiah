package core.domain

data class PlaylistTrack(
    val playlistTrack: Id<PlaylistTrack>? = null,
    val playlistId: Id<Playlist>? = null,
    val trackId: Id<Track>
)
