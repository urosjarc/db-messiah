package app.data

import com.urosjarc.dbmessiah.domain.Table
import core.domain.*


val artist_table = Table(Artist::artistId)
val mediaType_table = Table(MediaType::mediaTypeId)
val playlist_table = Table(Playlist::playlistId)
val genre_table = Table(Genre::genreId)

val album_table = Table(
    Album::albumId, foreignKeys = listOf(
        Album::artistId to Artist::class
    )
)
val track_table = Table(
    Track::trackId, foreignKeys = listOf(
        Track::genreId to Genre::class,
        Track::mediaTypeId to MediaType::class,
        Track::albumId to Album::class,
    )
)
val playlistTrack_table = Table(
    PlaylistTrack::playlistTrack, foreignKeys = listOf(
        PlaylistTrack::playlistId to Playlist::class,
        PlaylistTrack::trackId to Track::class
    )
)
val employee_table = Table(
    Employee::employeeId, foreignKeys = listOf(
        Employee::reportsTo to Employee::class
    )
)
val customer_table = Table(
    Customer::customerId, foreignKeys = listOf(
        Customer::supportRepId to Employee::class
    )
)
val invoice_table = Table(
    Invoice::invoiceId, foreignKeys = listOf(
        Invoice::customerId to Customer::class
    )
)
val invoiceLine_table = Table(
    InvoiceLine::invoiceLineId,
    foreignKeys = listOf(
        InvoiceLine::invoiceId to Invoice::class,
        InvoiceLine::trackId to Track::class
    )
)
