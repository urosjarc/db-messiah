package app.data

val music_schema = Pair(
    "music", listOf(
        artist_table,
        album_table,
        track_table,
        mediaType_table,
        playlist_table,
        playlistTrack_table,
        genre_table
    )
)

val billing_schema = Pair(
    "billing", listOf(
        invoice_table,
        invoiceLine_table
    )
)

val people_schema = Pair(
    "people", listOf(
        employee_table,
        customer_table
    )
)
