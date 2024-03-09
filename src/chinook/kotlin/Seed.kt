import domain.*
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.random.Random

/**
 * The [Seed] class is responsible for populating the database with sample data,
 * that is the same as famous [chinook sample database](https://github.com/lerocha/chinook-database).
 */
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
        repeat(num) { i ->
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
        val invoiceLine = arrayOfNulls<InvoiceLine>(num).mapIndexed { i, _ ->
            InvoiceLine(
                invoiceId = Id(Random.nextInt(1, num - 1)),
                trackId = Id(Random.nextInt(1, num - 1)),
                unitPrice = i.toFloat(),
                quantity = i
            )
        }

        db2.autocommit {
            it.schema.drop(schema = db2_people_schema, throws = false)
            it.schema.drop(schema = db2_music_schema, throws = false)
            it.schema.drop(schema = db2_billing_schema, throws = false)

            it.schema.create(schema = db2_people_schema)
            it.schema.create(schema = db2_music_schema)
            it.schema.create(schema = db2_billing_schema)

            it.table.create<Artist>()
            it.table.create<Album>()
            it.table.create<MediaType>()
            it.table.create<Genre>()
            it.table.create<Playlist>()
            it.table.create<Employee>()
            it.table.create<Customer>()
            it.table.create<Invoice>()
            it.table.create<Track>()
            it.table.create<PlaylistTrack>()
            it.table.create<InvoiceLine>()

            it.batch.insert(rows = artist)
            it.batch.insert(rows = artist)
            it.batch.insert(rows = album)
            it.batch.insert(rows = mediaType)
            it.batch.insert(rows = genre)
            it.batch.insert(rows = playlist)
            it.batch.insert(rows = employee)
            it.batch.insert(rows = customer)
            it.batch.insert(rows = invoice)
            it.batch.insert(rows = track)
            it.batch.insert(rows = playlistTrack)
            it.batch.insert(rows = invoiceLine)
        }
        derby.autocommit {
            it.schema.create(schema = derby_people_schema)
            it.schema.create(schema = derby_music_schema)
            it.schema.create(schema = derby_billing_schema)

            it.table.create<Artist>()
            it.table.create<Album>()
            it.table.create<MediaType>()
            it.table.create<Genre>()
            it.table.create<Playlist>()
            it.table.create<Employee>()
            it.table.create<Customer>()
            it.table.create<Invoice>()
            it.table.create<Track>()
            it.table.create<PlaylistTrack>()
            it.table.create<InvoiceLine>()

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
            it.batch.insert(invoiceLine)
        }
        h2.autocommit {
            it.schema.create(schema = h2_people_schema)
            it.schema.create(schema = h2_music_schema)
            it.schema.create(schema = h2_billing_schema)

            it.table.create<Artist>()
            it.table.create<Album>()
            it.table.create<MediaType>()
            it.table.create<Genre>()
            it.table.create<Playlist>()
            it.table.create<Employee>()
            it.table.create<Customer>()
            it.table.create<Invoice>()
            it.table.create<Track>()
            it.table.create<PlaylistTrack>()
            it.table.create<InvoiceLine>()

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
            it.batch.insert(invoiceLine)
        }
        maria.autocommit {
            it.schema.drop(schema = maria_billing_schema)
            it.schema.drop(schema = maria_music_schema)
            it.schema.drop(schema = maria_people_schema)

            it.schema.create(schema = maria_people_schema)
            it.schema.create(schema = maria_music_schema)
            it.schema.create(schema = maria_billing_schema)

            it.table.create<Artist>()
            it.table.create<Album>()
            it.table.create<MediaType>()
            it.table.create<Genre>()
            it.table.create<Playlist>()
            it.table.create<Employee>()
            it.table.create<Customer>()
            it.table.create<Invoice>()
            it.table.create<Track>()
            it.table.create<PlaylistTrack>()
            it.table.create<InvoiceLine>()

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
            it.batch.insert(invoiceLine)
        }
        mssql.autocommit {
            it.schema.create(schema = mssql_billing_schema)
            it.schema.create(schema = mssql_people_schema)
            it.schema.create(schema = mssql_music_schema)

            it.table.drop<InvoiceLine>()
            it.table.drop<PlaylistTrack>()
            it.table.drop<Track>()
            it.table.drop<Invoice>()
            it.table.drop<Customer>()
            it.table.drop<Employee>()
            it.table.drop<Playlist>()
            it.table.drop<Genre>()
            it.table.drop<MediaType>()
            it.table.drop<Album>()
            it.table.drop<Artist>()

            it.table.create<Artist>()
            it.table.create<Album>()
            it.table.create<MediaType>()
            it.table.create<Genre>()
            it.table.create<Playlist>()
//            it.table.create<Employee>() //Mssql does not allow circular foreign keys
//            it.table.create<Customer>()
//            it.table.create<Invoice>()
            it.table.create<Track>()
            it.table.create<PlaylistTrack>()
//            it.table.create<InvoiceLine>()

            it.batch.insert(artist)
            it.batch.insert(album)
            it.batch.insert(mediaType)
            it.batch.insert(genre)
            it.batch.insert(playlist)
//            it.batch.insert(employee)
//            it.batch.insert(customer)
//            it.batch.insert(invoice)
            it.batch.insert(track)
            it.batch.insert(playlistTrack)
//            it.batch.insert(invoiceLine)
        }
        mysql.autocommit {
            it.schema.create(schema = mysql_people_schema)
            it.schema.create(schema = mysql_music_schema)
            it.schema.create(schema = mysql_billing_schema)

            it.table.drop<InvoiceLine>()
            it.table.drop<PlaylistTrack>()
            it.table.drop<Track>()
            it.table.drop<Invoice>()
            it.table.drop<Customer>()
            it.table.drop<Employee>()
            it.table.drop<Playlist>()
            it.table.drop<Genre>()
            it.table.drop<MediaType>()
            it.table.drop<Album>()
            it.table.drop<Artist>()

            it.table.create<Artist>()
            it.table.create<Album>()
            it.table.create<MediaType>()
            it.table.create<Genre>()
            it.table.create<Playlist>()
            it.table.create<Employee>()
            it.table.create<Customer>()
            it.table.create<Invoice>()
            it.table.create<Track>()
            it.table.create<PlaylistTrack>()
            it.table.create<InvoiceLine>()

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
            it.batch.insert(invoiceLine)
        }
        mysql.autocommit {
            it.schema.drop(schema = mysql_billing_schema)
            it.schema.drop(schema = mysql_people_schema)
            it.schema.drop(schema = mysql_music_schema)

            it.schema.create(schema = mysql_people_schema)
            it.schema.create(schema = mysql_music_schema)
            it.schema.create(schema = mysql_billing_schema)

            it.table.create<Artist>()
            it.table.create<Album>()
            it.table.create<MediaType>()
            it.table.create<Genre>()
            it.table.create<Playlist>()
            it.table.create<Employee>()
            it.table.create<Customer>()
            it.table.create<Invoice>()
            it.table.create<Track>()
            it.table.create<PlaylistTrack>()
            it.table.create<InvoiceLine>()

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
            it.batch.insert(invoiceLine)
        }
        sqlite.autocommit {
            it.table.create<Artist>()
            it.table.create<Album>()
            it.table.create<MediaType>()
            it.table.create<Genre>()
            it.table.create<Playlist>()
            it.table.create<Employee>()
            it.table.create<Customer>()
            it.table.create<Invoice>()
            it.table.create<Track>()
            it.table.create<PlaylistTrack>()
            it.table.create<InvoiceLine>()

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
            it.batch.insert(invoiceLine)
        }
        pg.autocommit {
            it.schema.dropCascade(schema = pg_people_schema)
            it.schema.dropCascade(schema = pg_music_schema)
            it.schema.dropCascade(schema = pg_billing_schema)

            it.schema.create(schema = pg_people_schema)
            it.schema.create(schema = pg_music_schema)
            it.schema.create(schema = pg_billing_schema)

            it.table.create<Artist>()
            it.table.create<Album>()
            it.table.create<MediaType>()
            it.table.create<Genre>()
            it.table.create<Playlist>()
            it.table.create<Employee>()
            it.table.create<Customer>()
            it.table.create<Invoice>()
            it.table.create<Track>()
            it.table.create<PlaylistTrack>()
            it.table.create<InvoiceLine>()

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
            it.batch.insert(invoiceLine)
        }
        oracle.autocommit {
            it.table.drop<InvoiceLine>()
            it.table.drop<PlaylistTrack>()
            it.table.drop<Track>()
            it.table.drop<Invoice>()
            it.table.drop<Customer>()
            it.table.drop<Employee>()
            it.table.drop<Playlist>()
            it.table.drop<Genre>()
            it.table.drop<MediaType>()
            it.table.drop<Album>()
            it.table.drop<Artist>()

            it.table.create<Artist>()
            it.table.create<Album>()
            it.table.create<MediaType>()
            it.table.create<Genre>()
            it.table.create<Playlist>()
            it.table.create<Employee>()
            it.table.create<Customer>()
            it.table.create<Invoice>()
            it.table.create<Track>()
            it.table.create<PlaylistTrack>()
            it.table.create<InvoiceLine>()

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
            it.batch.insert(invoiceLine)
        }
    }

}
