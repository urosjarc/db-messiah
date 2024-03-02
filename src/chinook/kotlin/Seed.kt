import domain.*
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.random.Random

object Seed {
    fun all(num: Int) {
        val artist = arrayOfNulls<Album>(num).mapIndexed { i, _ -> Artist(name = "name_$i") }
        val album = arrayOfNulls<Album>(num).mapIndexed { i, _ -> Album(title = "name_$i", artistId = Id(Random.nextInt(1, num - 1))) }
        val mediaType = arrayOfNulls<MediaType>(num).mapIndexed { i, _ -> MediaType(name = "name_$i") }
        val genre = arrayOfNulls<Genre>(num).mapIndexed { i, _ -> Genre(name = "name_$i") }
        val playlist = arrayOfNulls<Playlist>(num).mapIndexed { i, _ -> Playlist(name = "name_$i") }
        val employee = mutableListOf(
            Employee(
                firstName = "firstName",
                lastName = "lastName",
                title = "title",
                reportsTo = null,
                birthDate = Clock.System.now().toLocalDateTime(TimeZone.UTC).date,
                hireDate = Clock.System.now().toLocalDateTime(TimeZone.UTC).date,
            )
        )
        repeat(19) { i ->
            employee.add(
                Employee(
                    firstName = "firstName_$i",
                    lastName = "lastName_$i",
                    title = "title_$i",
                    reportsTo = Id(1),
                    birthDate = Clock.System.now().toLocalDateTime(TimeZone.UTC).date,
                    hireDate = Clock.System.now().toLocalDateTime(TimeZone.UTC).date,
                )
            )
        }

        val customer = arrayOfNulls<Customer>(num).mapIndexed { i, _ ->
            Customer(
                firstName = "firstName_$i",
                lastName = "lastName_$i",
                supportRepId = Id(Random.nextInt(1, num - 1))
            )
        }

        val invoice = arrayOfNulls<Invoice>(num).mapIndexed { i, _ ->
            Invoice(
                customerId = Id(Random.nextInt(1, num - 1)),
                invoiceDate = Clock.System.now().toLocalDateTime(TimeZone.UTC).date,
                billingAddress = "billingAddress_$i"
            )
        }

        val track = arrayOfNulls<Track>(num).mapIndexed { i, _ ->
            Track(
                name = "name_$i",
                albumId = Id(Random.nextInt(1, num - 1)),
                mediaTypeId = Id(Random.nextInt(1, num - 1)),
                genreId = Id(Random.nextInt(1, num - 1)),
                composer = "composer_id"
            )
        }

        val playlistTrack = arrayOfNulls<PlaylistTrack>(num).mapIndexed { i, _ ->
            PlaylistTrack(
                playlistId = Id(Random.nextInt(1, num - 1)),
                trackId = Id(Random.nextInt(1, num - 1)),
            )
        }

        sqlite.autocommit {
            it.table.create(table = Artist::class)
            it.table.create(table = Album::class)
            it.table.create(table = MediaType::class)
            it.table.create(table = Genre::class)
            it.table.create(table = Playlist::class)
            it.table.create(table = Employee::class)
            it.table.create(table = Customer::class)
            it.table.create(table = Invoice::class)
            it.table.create(table = Track::class)
            it.table.create(table = PlaylistTrack::class)

            it.batch.insert(artist)
            it.batch.insert(album)
            it.batch.insert(mediaType)
            it.batch.insert(genre)
            it.batch.insert(playlist)
            it.batch.insert(employee)
            it.batch.insert(customer)
            it.batch.insert(invoice)
            it.batch.insert(track)
            it.batch.insert(playlistTrack)
        }

        pg.autocommit {
            it.schema.dropCascade(schema = pg_people_schema)
            it.schema.dropCascade(schema = pg_music_schema)
            it.schema.dropCascade(schema = pg_billing_schema)

            it.schema.create(schema = pg_people_schema)
            it.schema.create(schema = pg_music_schema)
            it.schema.create(schema = pg_billing_schema)

            it.table.create(table = Artist::class)
            it.table.create(table = Album::class)
            it.table.create(table = MediaType::class)
            it.table.create(table = Genre::class)
            it.table.create(table = Playlist::class)
            it.table.create(table = Employee::class)
            it.table.create(table = Customer::class)
            it.table.create(table = Invoice::class)
            it.table.create(table = Track::class)
            it.table.create(table = PlaylistTrack::class)

            it.batch.insert(artist)
            it.batch.insert(album)
            it.batch.insert(mediaType)
            it.batch.insert(genre)
            it.batch.insert(playlist)
            it.batch.insert(employee)
            it.batch.insert(customer)
            it.batch.insert(invoice)
            it.batch.insert(track)
            it.batch.insert(playlistTrack)
        }

    }

}
